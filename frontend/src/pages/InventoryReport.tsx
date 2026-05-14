import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Card, Row, Col, Table, Tag, Tabs, Statistic, Button, message } from 'antd';
import { inventoryRecordApi } from '../api/inventory';

const resultColors: Record<string, string> = {
  NORMAL: 'success',
  SURPLUS: 'processing',
  MISSING: 'error',
  PENDING: 'default',
};
const resultLabels: Record<string, string> = {
  NORMAL: '正常',
  SURPLUS: '盘盈',
  MISSING: '盘亏',
  PENDING: '待确认',
};

export default function InventoryReport() {
  const { planId } = useParams<{ planId: string }>();
  const [report, setReport] = useState<any>(null);
  const [records, setRecords] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('all');

  useEffect(() => {
    if (!planId) return;
    loadReport();
  }, [planId]);

  const loadReport = async () => {
    if (!planId) return;
    setLoading(true);
    try {
      const r = await inventoryRecordApi.report(Number(planId));
      setReport(r);
      const all = await inventoryRecordApi.list(Number(planId));
      setRecords(Array.isArray(all) ? all : []);
    } finally {
      setLoading(false);
    }
  };

  const filteredRecords = activeTab === 'all' ? records
    : records.filter(r => r.result === activeTab.toUpperCase());

  const handleExport = async () => {
    if (!planId) return;
    try {
      const blob = await inventoryRecordApi.export(Number(planId));
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `inventory_report_${planId}.xlsx`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch {
      message.error('导出失败');
    }
  };

  const columns = [
    { title: '资产编码', dataIndex: 'assetCode' },
    { title: '资产名称', dataIndex: 'assetName' },
    { title: '部门', dataIndex: 'departmentName' },
    {
      title: '结果',
      dataIndex: 'result',
      render: (r: string) => <Tag color={resultColors[r]}>{resultLabels[r] || r}</Tag>,
    },
    { title: '盘点人', dataIndex: 'checkedByName' },
    { title: '盘点时间', dataIndex: 'checkedAt' },
    { title: '备注', dataIndex: 'remark' },
  ];

  if (!report) return null;

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16, justifyContent: 'flex-end' }}>
        <Col>
          <Button type="primary" onClick={handleExport}>导出Excel</Button>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={4}>
          <Card><Statistic title="应盘资产" value={report.totalAssets} /></Card>
        </Col>
        <Col span={4}>
          <Card><Statistic title="已盘资产" value={report.checkedAssets} /></Card>
        </Col>
        <Col span={4}>
          <Card><Statistic title="正常" value={report.normalCount} valueStyle={{ color: '#52c41a' }} /></Card>
        </Col>
        <Col span={4}>
          <Card><Statistic title="盘盈" value={report.surplusCount} valueStyle={{ color: '#1890ff' }} /></Card>
        </Col>
        <Col span={4}>
          <Card><Statistic title="盘亏" value={report.missingCount} valueStyle={{ color: '#ff4d4f' }} /></Card>
        </Col>
        <Col span={4}>
          <Card><Statistic title="完成率" suffix="%" value={report.completionRate} /></Card>
        </Col>
      </Row>

      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={[
          { key: 'all', label: `全部 (${records.length})` },
          { key: 'NORMAL', label: `正常 (${report.normalCount})` },
          { key: 'SURPLUS', label: `盘盈 (${report.surplusCount})` },
          { key: 'MISSING', label: `盘亏 (${report.missingCount})` },
        ]}
      />

      <Table columns={columns} dataSource={filteredRecords} rowKey="id" loading={loading} />
    </div>
  );
}
