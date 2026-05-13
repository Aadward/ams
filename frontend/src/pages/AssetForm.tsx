import { Form, Input, Select, DatePicker, InputNumber, Button, Card, Space, message } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAsset, useCreateAsset, useUpdateAsset } from '../api/asset';

const { Option } = Select;

export default function AssetForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const isEdit = !!id;
  const numericId = Number(id);

  const { data: asset, isLoading } = useAsset(numericId);
  const createMutation = useCreateAsset();
  const updateMutation = useUpdateAsset(numericId);

  useEffect(() => {
    if (isEdit && asset) {
      form.setFieldsValue({
        assetCode: asset.assetCode,
        name: asset.name,
        category: asset.category,
        spec: asset.spec,
        location: asset.location,
        purchaseDate: asset.purchaseDate,
        purchasePrice: asset.purchasePrice,
        warrantyEnd: asset.warrantyEnd,
        supplier: asset.supplier,
      });
    }
  }, [isEdit, asset, form]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      if (isEdit) {
        await updateMutation.mutateAsync(values);
        message.success('更新成功');
      } else {
        await createMutation.mutateAsync(values);
        message.success('创建成功');
      }
      navigate('/assets');
    } catch {
      message.error('操作失败');
    }
  };

  return (
    <Card title={isEdit ? '编辑资产' : '新建资产'} loading={isEdit && isLoading}>
      <Form
        form={form}
        layout="vertical"
        style={{ maxWidth: 600 }}
        onFinish={handleSubmit}
      >
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
        <Form.Item label="购入日期" name="purchaseDate">
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item label="价格" name="purchasePrice">
          <InputNumber min={0} style={{ width: '100%' }} addonAfter="元" />
        </Form.Item>
        <Form.Item label="保修截止" name="warrantyEnd">
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item label="供应商" name="supplier">
          <Input />
        </Form.Item>
        <Form.Item label="位置" name="location">
          <Input />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>
              {isEdit ? '保存' : '创建'}
            </Button>
            <Button onClick={() => navigate('/assets')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}