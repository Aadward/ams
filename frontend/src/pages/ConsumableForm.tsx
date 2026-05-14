import { useEffect } from 'react';
import { Form, Input, Select, InputNumber, Button, Card, message } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { consumableApi } from '../api/consumable';

export default function ConsumableForm() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  useEffect(() => {
    if (isEdit) {
      consumableApi.getById(Number(id)).then(data => {
        form.setFieldsValue({
          name: data.name,
          category: data.category,
          spec: data.spec,
          unit: data.unit,
          threshold: data.threshold,
        });
      });
    }
  }, [id]);

  const handleSubmit = async (values: any) => {
    try {
      if (isEdit) {
        await consumableApi.update(Number(id), values);
        message.success('更新成功');
      } else {
        await consumableApi.create(values);
        message.success('创建成功');
      }
      navigate('/consumables');
    } catch (e: any) {
      message.error(e.message || '操作失败');
    }
  };

  return (
    <Card title={isEdit ? '编辑易耗品' : '新增易耗品'}>
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="category" label="分类" rules={[{ required: true, message: '请选择分类' }]}>
          <Select>
            <Select.Option value="OFFICE_SUPPLIES">办公用品</Select.Option>
            <Select.Option value="ELECTRONIC_PARTS">电子配件</Select.Option>
            <Select.Option value="PRODUCTION_CONSUMABLES">生产耗材</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="spec" label="规格型号">
          <Input />
        </Form.Item>
        <Form.Item name="unit" label="单位" rules={[{ required: true, message: '请输入单位' }]}>
          <Input placeholder="如：个、箱、卷" />
        </Form.Item>
        <Form.Item name="threshold" label="预警阈值" initialValue={10}>
          <InputNumber min={0} />
        </Form.Item>
        <Button type="primary" htmlType="submit">提交</Button>
      </Form>
    </Card>
  );
}