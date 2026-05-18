import { useState } from 'react';
import { Form, Input, Select, InputNumber, Button, Card, Space, message, Row, Col, Radio } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useCreateProcurement } from '../api/procurement';
import { useAssetList } from '../api/asset';

const { Option } = Select;
const { TextArea } = Input;

export default function ProcurementApply() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [procurementType, setProcurementType] = useState<'PURCHASE' | 'CONSUMABLE'>('PURCHASE');

  const { data: assetData } = useAssetList({ status: 'IN_STOCK', size: 1000 });
  const createProcurement = useCreateProcurement();

  const handleSubmit = async (values: {
    assetId?: number;
    type: 'PURCHASE' | 'CONSUMABLE';
    name: string;
    description?: string;
    quantity: number;
    unitPrice: number;
    reason?: string;
  }) => {
    setSubmitting(true);
    try {
      await createProcurement.mutateAsync({
        assetId: values.assetId,
        type: values.type,
        name: values.name,
        description: values.description,
        quantity: values.quantity,
        unitPrice: values.unitPrice,
        reason: values.reason,
      });
      message.success('申请提交成功');
      navigate('/procurements');
    } catch {
      message.error('提交失败');
    } finally {
      setSubmitting(false);
    }
  };

  const selectedAssetId = Form.useWatch('assetId', form);
  const selectedAsset = assetData?.content?.find(a => a.id === selectedAssetId);

  return (
    <Card title="采购申请">
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Form.Item
          name="type"
          label="采购类型"
          initialValue="PURCHASE"
          rules={[{ required: true }]}
        >
          <Radio.Group onChange={(e) => setProcurementType(e.target.value)} value={procurementType}>
            <Radio value="PURCHASE">资产采购</Radio>
            <Radio value="CONSUMABLE">耗材采购</Radio>
          </Radio.Group>
        </Form.Item>

        {procurementType === 'PURCHASE' && (
          <Row gutter={24}>
            <Col span={12}>
              <Form.Item
                name="assetId"
                label="关联资产"
              >
                <Select
                  showSearch
                  allowClear
                  placeholder="搜索选择资产（可选）"
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
        )}

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
              name="name"
              label="采购名称"
              rules={[{ required: true, message: '请输入采购名称' }]}
            >
              <Input placeholder="请输入采购名称" />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={24}>
          <Col span={8}>
            <Form.Item
              name="quantity"
              label="数量"
              rules={[{ required: true, message: '请输入数量' }]}
            >
              <InputNumber min={1} style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item
              name="unitPrice"
              label="单价（元）"
              rules={[{ required: true, message: '请输入单价' }]}
            >
              <InputNumber min={0} precision={2} style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item label="总价">
              <InputNumber
                disabled
                value={(() => {
                  const qty = form.getFieldValue('quantity');
                  const price = form.getFieldValue('unitPrice');
                  return qty && price ? qty * price : 0;
                })()}
                precision={2}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={24}>
          <Col span={24}>
            <Form.Item name="description" label="采购说明">
              <TextArea rows={4} placeholder="请输入采购说明" />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={24}>
          <Col span={24}>
            <Form.Item name="reason" label="申请原因">
              <TextArea rows={3} placeholder="请输入申请原因" />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={submitting}>提交申请</Button>
            <Button onClick={() => navigate('/procurements')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}
