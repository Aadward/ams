import { useEffect } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { supplierApi } from '../api/supplier';

export default function SupplierForm() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  useEffect(() => {
    if (isEdit) {
      supplierApi.getById(Number(id)).then(res => {
        const data = res.data;
        form.setFieldsValue({
          name: data.name,
          contactPerson: data.contactPerson,
          phone: data.phone,
          email: data.email,
          address: data.address,
          remark: data.remark,
        });
      }).catch(() => {
        message.error('加载数据失败');
      });
    }
  }, [id]);

  const handleSubmit = async (values: any) => {
    try {
      if (isEdit) {
        await supplierApi.update(Number(id), values);
        message.success('更新成功');
      } else {
        await supplierApi.create(values);
        message.success('创建成功');
      }
      navigate('/suppliers');
    } catch {
      message.error('操作失败');
    }
  };

  return (
    <Card title={isEdit ? '编辑供应商' : '新增供应商'}>
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Form.Item name="name" label="供应商名称" rules={[{ required: true, message: '请输入供应商名称' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="contactPerson" label="联系人">
          <Input />
        </Form.Item>
        <Form.Item name="phone" label="电话">
          <Input />
        </Form.Item>
        <Form.Item name="email" label="邮箱">
          <Input />
        </Form.Item>
        <Form.Item name="address" label="地址">
          <Input />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Button type="primary" htmlType="submit">提交</Button>
      </Form>
    </Card>
  );
}
