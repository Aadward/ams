import { useState } from 'react';
import { Form, Input, Select, DatePicker, Button, Card, Space, message, Row, Col } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useCreateBorrow } from '../api/borrow';
import { useAssetList } from '../api/asset';
import { useEmployeeList } from '../api/employee';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

export default function BorrowApply() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  const { data: assetData } = useAssetList({ status: 'IN_STOCK', size: 1000 });
  const { data: employeeData } = useEmployeeList();
  const createBorrow = useCreateBorrow();

  const handleSubmit = async (values: {
    assetId: number;
    borrowerId: number;
    departmentId: number;
    expectedReturnDate?: dayjs.Dayjs;
    reason?: string;
  }) => {
    setSubmitting(true);
    try {
      await createBorrow.mutateAsync({
        assetId: values.assetId,
        borrowerId: values.borrowerId,
        departmentId: values.departmentId,
        expectedReturnDate: values.expectedReturnDate?.format('YYYY-MM-DD'),
        reason: values.reason,
      });
      message.success('申请提交成功');
      navigate('/borrows');
    } catch {
      message.error('提交失败');
    } finally {
      setSubmitting(false);
    }
  };

  const selectedAssetId = Form.useWatch('assetId', form);
  const selectedAsset = assetData?.content?.find(a => a.id === selectedAssetId);

  return (
    <Card title="资产借用申请">
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Row gutter={24}>
          <Col span={12}>
            <Form.Item
              name="assetId"
              label="资产"
              rules={[{ required: true, message: '请选择资产' }]}>
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
              name="borrowerId"
              label="借用人"
              rules={[{ required: true, message: '请选择借用人' }]}
            >
              <Select
                showSearch
                placeholder="选择员工"
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
          <Col span={12}>
            <Form.Item
              name="departmentId"
              label="部门"
              rules={[{ required: true, message: '请选择部门' }]}
            >
              <Select placeholder="选择部门">
                {employeeData?.content?.map(emp => (
                  <Option key={emp.deptId} value={emp.deptId}>
                    {emp.deptName}
                  </Option>
                )).filter((v, i, a) => a.findIndex(t => t?.key === v?.key) === i)}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={24}>
          <Col span={12}>
            <Form.Item
              name="expectedReturnDate"
              label="预计归还日期"
              rules={[{ required: true, message: '请选择预计归还日期' }]}
            >
              <DatePicker style={{ width: '100%' }} disabledDate={(current) => current && current < dayjs().startOf('day')} />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={24}>
          <Col span={24}>
            <Form.Item name="reason" label="借用原因">
              <TextArea rows={4} placeholder="请输入借用原因" />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={submitting}>提交申请</Button>
            <Button onClick={() => navigate('/borrows')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}
