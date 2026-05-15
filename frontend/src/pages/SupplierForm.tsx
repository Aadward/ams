import { useEffect } from 'react';
import { Form, Input, Button, Card, message, Select } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { supplierApi } from '../api/supplier';

const SUPPLIER_TYPE_OPTIONS = [
  { value: 'EQUIPMENT', label: '设备供应商' },
  { value: 'CONSUMABLE', label: '易耗品供应商' },
  { value: 'MAINTENANCE', label: '维修服务商' },
  { value: 'MULTI', label: '多元化供应商' },
];

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
          supplierCode: data.supplierCode,
          name: data.name,
          type: data.type,
          contactPerson: data.contact,
          phone: data.phone,
          email: data.email,
          address: data.address,
          remark: data.remark,
          status: data.status,
          rating: data.rating,
        });
      }).catch(() => {
        message.error('加载数据失败');
      });
    }
  }, [id]);

  const handleSubmit = async (values: any) => {
    try {
      // Transform contactPerson -> contact (backend field name)
      const payload = {
        ...values,
        contact: values.contactPerson,
      };
      delete payload.contactPerson;
      if (isEdit) {
        await supplierApi.update(Number(id), payload);
        message.success('更新成功');
      } else {
        await supplierApi.create(payload);
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
        <Form.Item name="supplierCode" label="供应商编码" rules={[{ required: true, message: '请输入供应商编码' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="type" label="供应商类型" rules={[{ required: true, message: '请选择供应商类型' }]}>
          <Select options={SUPPLIER_TYPE_OPTIONS} />
        </Form.Item>
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
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Select options={[{ value: 'ACTIVE', label: '启用' }, { value: 'INACTIVE', label: '停用' }]} />
        </Form.Item>
        <Form.Item name="rating" label="评级">
          <Select options={[
            { value: 1, label: '★' },
            { value: 2, label: '★★' },
            { value: 3, label: '★★★' },
            { value: 4, label: '★★★★' },
            { value: 5, label: '★★★★★' },
          ]} />
        </Form.Item>
        <Button type="primary" htmlType="submit">提交</Button>
      </Form>
    </Card>
  );
}
