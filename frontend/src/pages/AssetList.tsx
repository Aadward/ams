import { Table, Button, Space, Select, Tag, Modal, Input, message, Dropdown, Upload } from 'antd';
import { PrinterOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAssetList, useBatchAssign, useBatchUnassign, useBatchRetire, useBatchUpdateLocation } from '../api/asset';
import { useEmployeeList } from '../api/employee';
import http from '../api/http';

const { Option } = Select;

const categoryBadgeColor: Record<string, string> = {
  HARDWARE: 'blue',
  NETWORK: 'green',
  PERIPHERAL: 'orange',
  SOFTWARE_LICENSE: 'purple',
};

const categoryLabel: Record<string, string> = {
  HARDWARE: '硬件设备',
  NETWORK: '网络设备',
  PERIPHERAL: '配件耗材',
  SOFTWARE_LICENSE: '软件许可证',
};

const statusBadgeColor: Record<string, string> = {
  IN_STOCK: 'green',
  IN_USE: 'blue',
  MAINTENANCE: 'orange',
  RETIRED: 'red',
};

const statusLabel: Record<string, string> = {
  IN_STOCK: '库存',
  IN_USE: '已领用',
  MAINTENANCE: '维修中',
  RETIRED: '已报废',
};

export default function AssetList() {
  const navigate = useNavigate();
  const [category, setCategory] = useState<string>();
  const [status, setStatus] = useState<string>();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [batchModal, setBatchModal] = useState<{ type: string; open: boolean }>({ type: '', open: false });
  const [batchLocation, setBatchLocation] = useState('');
  const [batchAssignId, setBatchAssignId] = useState<number | null>(null);
  const [importModalOpen, setImportModalOpen] = useState(false);
  const { data, isLoading } = useAssetList({ category, status });
  const { data: employees } = useEmployeeList();
  const batchAssign = useBatchAssign();
  const batchUnassign = useBatchUnassign();
  const batchRetire = useBatchRetire();
  const batchUpdateLocation = useBatchUpdateLocation();

  const handleBatchAction = (type: string) => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先选择资产');
      return;
    }
    setBatchModal({ type, open: true });
  };

  const executeBatch = async () => {
    const ids = selectedRowKeys as number[];
    try {
      if (batchModal.type === 'assign' && batchAssignId) {
        await batchAssign.mutateAsync({ assetIds: ids, assigneeId: batchAssignId });
        message.success(`成功领用 ${ids.length} 个资产`);
      } else if (batchModal.type === 'unassign') {
        const result = await batchUnassign.mutateAsync(ids);
        message.success(`成功归还 ${result.count} 个资产`);
      } else if (batchModal.type === 'retire') {
        const result = await batchRetire.mutateAsync(ids);
        message.success(`成功报废 ${result.count} 个资产`);
      } else if (batchModal.type === 'location') {
        const result = await batchUpdateLocation.mutateAsync({ assetIds: ids, location: batchLocation });
        message.success(`成功更新 ${result.count} 个资产位置`);
      }
      setBatchModal({ type: '', open: false });
      setSelectedRowKeys([]);
      setBatchLocation('');
      setBatchAssignId(null);
    } catch {
      message.error('操作失败');
    }
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => setSelectedRowKeys(keys),
  };

  const batchMenuItems = [
    { key: 'assign', label: '批量领用' },
    { key: 'unassign', label: '批量归还' },
    { key: 'retire', label: '批量报废' },
    { key: 'location', label: '批量更新位置' },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Space>
        <Select placeholder="分类" allowClear style={{ width: 160 }} value={category} onChange={setCategory}>
          <Option value="HARDWARE">硬件设备</Option>
          <Option value="NETWORK">网络设备</Option>
          <Option value="PERIPHERAL">配件耗材</Option>
          <Option value="SOFTWARE_LICENSE">软件许可证</Option>
        </Select>
        <Select placeholder="状态" allowClear style={{ width: 140 }} value={status} onChange={setStatus}>
          <Option value="IN_STOCK">库存</Option>
          <Option value="IN_USE">已领用</Option>
          <Option value="MAINTENANCE">维修中</Option>
          <Option value="RETIRED">已报废</Option>
        </Select>
        <Button type="primary" onClick={() => navigate('/assets/new')}>新建资产</Button>
        <Button onClick={() => { window.open('/api/assets/export', '_blank'); }}>导出 Excel</Button>
        <Button onClick={() => setImportModalOpen(true)}>导入 Excel</Button>
        <Dropdown menu={{ items: batchMenuItems, onClick: ({ key }) => handleBatchAction(key) }} disabled={selectedRowKeys.length === 0}>
          <Button>批量操作 ({selectedRowKeys.length})</Button>
        </Dropdown>
      </Space>

      <Table
        loading={isLoading}
        dataSource={data?.content}
        rowKey="id"
        rowSelection={rowSelection}
        pagination={{ total: data?.totalElements, defaultPageSize: 10 }}
        onRow={(record) => ({
          onClick: () => navigate(`/assets/${record.id}`),
          style: { cursor: 'pointer' },
        })}
        columns={[
          { title: '资产编码', dataIndex: 'assetCode' },
          { title: '名称', dataIndex: 'name' },
          {
            title: '分类',
            dataIndex: 'category',
            render: (val: string) => (
              <Tag color={categoryBadgeColor[val]}>{categoryLabel[val] || val}</Tag>
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            render: (val: string) => (
              <Tag color={statusBadgeColor[val]}>{statusLabel[val] || val}</Tag>
            ),
          },
          { title: '购入日期', dataIndex: 'purchaseDate' },
          { title: '价格', dataIndex: 'purchasePrice' },
          { title: '位置', dataIndex: 'location' },
          {
            title: '操作',
            render: (_: unknown, record: { id: number; assetCode: string }) => (
              <Space>
                <Button size="small" onClick={(e) => { e.stopPropagation(); navigate(`/assets/${record.id}`); }}>查看</Button>
                <Button size="small" onClick={(e) => { e.stopPropagation(); navigate(`/assets/${record.id}/edit`); }}>编辑</Button>
                <Button size="small" icon={<PrinterOutlined />} onClick={(e) => {
                  e.stopPropagation();
                  // Open print URL in new tab (auth handled via HTTP-only cookie or query token)
                  const printUrl = `${import.meta.env.VITE_API_URL || ''}/api/asset-tags/${record.id}/print`;
                  window.open(printUrl, '_blank');
                }}>打印标签</Button>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        title={
          batchModal.type === 'assign' ? '批量领用' :
          batchModal.type === 'unassign' ? '批量归还' :
          batchModal.type === 'retire' ? '批量报废' : '批量更新位置'
        }
        open={batchModal.open}
        onOk={executeBatch}
        onCancel={() => { setBatchModal({ type: '', open: false }); setBatchLocation(''); setBatchAssignId(null); }}
        confirmLoading={batchAssign.isPending || batchUnassign.isPending || batchRetire.isPending || batchUpdateLocation.isPending}
      >
        {batchModal.type === 'assign' && (
          <div>
            <p>将 {selectedRowKeys.length} 个资产批量领用给：</p>
            <Select
              style={{ width: '100%' }}
              placeholder="选择员工"
              value={batchAssignId}
              onChange={setBatchAssignId}
            >
              {employees?.content?.map(emp => (
                <Option key={emp.id} value={emp.id}>{emp.name} {emp.deptName && `(${emp.deptName})`}</Option>
              ))}
            </Select>
          </div>
        )}
        {batchModal.type === 'location' && (
          <div>
            <p>将 {selectedRowKeys.length} 个资产的位置更新为：</p>
            <Input value={batchLocation} onChange={e => setBatchLocation(e.target.value)} placeholder="请输入位置" />
          </div>
        )}
        {(batchModal.type === 'unassign' || batchModal.type === 'retire') && (
          <p>确定要{batchModal.type === 'unassign' ? '归还' : '报废'}这 {selectedRowKeys.length} 个资产吗？</p>
        )}
      </Modal>

      <Modal title="导入 Excel" open={importModalOpen} footer={null} onCancel={() => setImportModalOpen(false)}>
        <p>上传 Excel 文件（.xlsx 格式）导入资产数据：</p>
        <Upload
          accept=".xlsx,.xls"
          showUploadList={false}
          beforeUpload={async (file) => {
            const formData = new FormData();
            formData.append('file', file);
            try {
              const res = await http.post('/assets/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
              const data = res.data as { successCount?: number; skipCount?: number; errors?: string[] };
              message.success(`成功导入 ${data.successCount || 0} 条数据`);
              if (data.errors && data.errors.length > 0) {
                message.warning(`有 ${data.errors.length} 条错误`);
              }
              setImportModalOpen(false);
            } catch (err) {
              message.error('导入失败');
            }
            return false;
          }}
        >
          <Button>选择文件</Button>
        </Upload>
      </Modal>
    </Space>
  );
}
