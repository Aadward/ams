import { useState } from 'react';
import { Form, Input, Select, Button, Card, Space, message, Row, Col, Radio } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useCreateTransfer } from '../api/transfer';
import { useAssetList } from '../api/asset';
import { useEmployeeList } from '../api/employee';
import { useDepartmentList } from '../api/department';

const { Option } = Select;
const { TextArea } = Input;

export default function TransferApply() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [transferType, setTransferType] = useState<'DEPARTMENT' | 'PERSON'>('DEPARTMENT');

  const { data: assetData } = useAssetList({ status: 'IN_USE', size: 1000 });
  const { data: employeeData } = useEmployeeList();
  const { data: deptData } = useDepartmentList();
  const createTransfer = useCreateTransfer();

  const handleSubmit = async (values: {
    assetId: number;
    fromDepartmentId: number;
    toDepartmentId: number;
    toPersonId?: number;
    reason?: string;
  }) => {
    setSubmitting(true);
    try {
      await createTransfer.mutateAsync({
        assetId: values.assetId,
        fromDepartmentId: values.fromDepartmentId,
        toDepartmentId: values.toDepartmentId,
        transferType,
        toPersonId: values.toPersonId,
        reason: values.reason,
      });
      message.success('申请提交成功');
      navigate('/transfers');
    } catch {
      message.error('提交失败');
    } finally {
      setSubmitting(false);
    }
  };

  const selectedAssetId = Form.useWatch('assetId', form);
  const selectedAsset = assetData?.content?.find(a => a.id === selectedAssetId);

  return (
    <Card title="资产调拨申请">
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Form.Item
          name="transferType"
          label="调拨类型"
          initialValue="DEPARTMENT"
          rules={[{ required: true }]}
        >
          <Radio.Group onChange={(e) => setTransferType(e.target.value)} value={transferType}>
            <Radio value="DEPARTMENT">部门调拨</Radio>
            <Radio value="PERSON">人员调拨</Radio>
          </Radio.Group>
        </Form.Item>

        <Row gutter={24}>
          <Col span={12}>
            <Form.Item
              name="assetId"
              label="资产"
              rules={[{ required: true, message: '请选择资产' }]}
            >
              <Select
                showSearch
                placeholder="搜索选择资产"
                optionFilterProp="children"
                filterOption={(input, option) =>
                  String(option?.children || '').toLowerCase().includes(input.toLowerCase())
                }
              >
                {assetData?.content?.map(asset => (
                  <Option key={asset.id} value={asset.id}>
                    {asset.assetCode} - {asset.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        {selectedAsset && (
          <Card size="small" style={{ marginBottom: 24, background: '#f5f5f5' }}>
            <Row gutter={16}>
              <Col span={8}><strong>资产编码：</strong>{selectedAsset.assetCode}</Col>
              <Col span={8}><strong>名称：</strong>{selectedAsset.name}</Col>
              <Col span={8}><strong>分类：</strong>{selectedAsset.category}</Col>
            </Row>
          </Card>
        )}

        <Row gutter={24}>
          <Col span={12}>
            <Form.Item
              name="fromDepartmentId"
              label="调出部门"
              rules={[{ required: true, message: '请选择调出部门' }]}
            >
              <Select placeholder="选择调出部门" showSearch optionFilterProp="children">
                {deptData?.map(dept => (
                  <Option key={dept.id} value={dept.id}>
                    {dept.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="toDepartmentId"
              label="调入部门"
              rules={[{ required: true, message: '请选择调入部门' }]}
            >
              <Select placeholder="选择调入部门" showSearch optionFilterProp="children">
                {deptData?.map(dept => (
                  <Option key={dept.id} value={dept.id}>
                    {dept.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        {transferType === 'PERSON' && (
          <Row gutter={24}>
            <Col span={12}>
              <Form.Item
                name="toPersonId"
                label="调入人员"
                rules={[{ required: true, message: '请选择调入人员' }]}
              >
                <Select
                  showSearch
                  placeholder="选择人员"
                  optionFilterProp="children"
                  filterOption={(input, option) =>
                    (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
                  }
                >
                  {employeeData?.content?.map(emp => (
                    <Option key={emp.id} value={emp.id}>
                      {emp.name} {emp.deptName && `(${emp.deptName})`}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>
        )}

        <Row gutter={24}>
          <Col span={24}>
            <Form.Item name="reason" label="调拨原因">
              <TextArea rows={4} placeholder="请输入调拨原因" />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={submitting}>提交申请</Button>
            <Button onClick={() => navigate('/transfers')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}