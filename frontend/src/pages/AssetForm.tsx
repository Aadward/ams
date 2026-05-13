import { Form, Input, Select, DatePicker, InputNumber, Button, Card } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';

const { Option } = Select;

export default function AssetForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id;

  return (
    <Card title={isEdit ? '编辑资产' : '新建资产'}>
      <Form layout="vertical" style={{ maxWidth: 600 }}>
        <Form.Item label="资产编码" name="assetCode" rules={[{ required: true }]}>
          <Input placeholder="如 PC-2024-0001" />
        </Form.Item>
        <Form.Item label="名称" name="name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item label="分类" name="category" rules={[{ required: true }]}>
          <Select>
            <Option value="HARDWARE">硬件设备</Option>
            <Option value="NETWORK">网络设备</Option>
            <Option value="PERIPHERAL">配件耗材</Option>
            <Option value="SOFTWARE_LICENSE">软件许可证</Option>
          </Select>
        </Form.Item>
        <Form.Item label="规格" name="spec">
          <Input.TextArea />
        </Form.Item>
        <Form.Item label="存放地点" name="location">
          <Input />
        </Form.Item>
        <Form.Item label="采购日期" name="purchaseDate">
          <DatePicker />
        </Form.Item>
        <Form.Item label="采购价格" name="purchasePrice">
          <InputNumber min={0} style={{ width: 200 }} addonAfter="元" />
        </Form.Item>
        <Form.Item label="保修到期" name="warrantyEnd">
          <DatePicker />
        </Form.Item>
        <Form.Item label="供应商" name="supplier">
          <Input />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">{isEdit ? '保存' : '创建'}</Button>
            <Button onClick={() => navigate('/assets')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}
