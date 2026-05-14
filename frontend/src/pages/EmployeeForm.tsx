import { Form, Input, Button, Card, Space, message, Select } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useEmployee, useCreateEmployee, useUpdateEmployee } from '../api/employee';
import { useDepartmentList } from '../api/department';

export default function EmployeeForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const isEdit = !!id;
  const numericId = Number(id);

  const { data: employee, isLoading } = useEmployee(numericId);
  useDepartmentList();
  const createMutation = useCreateEmployee();
  const updateMutation = useUpdateEmployee();

  useEffect(() => {
    if (isEdit && employee) {
      form.setFieldsValue({
        name: employee.name,
        deptId: employee.deptId,
        deptName: employee.deptName,
        email: employee.email,
        phone: employee.phone,
        role: employee.role,
      });
    }
  }, [isEdit, employee, form]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      if (isEdit) {
        await updateMutation.mutateAsync(values);
        message.success('更新成功');
      } else {
        await createMutation.mutateAsync(values);
        message.success('创建成功');
      }
      navigate('/employees');
    } catch {
      message.error('操作失败');
    }
  };

  return (
    <Card title={isEdit ? '编辑员工' : '新建员工'} loading={isEdit && isLoading}>
      <Form
        form={form}
        layout="vertical"
        style={{ maxWidth: 600 }}
        onFinish={handleSubmit}
      >
        <Form.Item label="姓名" name="name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item label="部门" name="deptId">
          <Input type="number" placeholder="部门 ID" />
        </Form.Item>
        <Form.Item label="部门名称" name="deptName">
          <Input placeholder="部门名称（冗余字段）" />
        </Form.Item>
        <Form.Item label="邮箱" name="email">
          <Input type="email" />
        </Form.Item>
        <Form.Item label="电话" name="phone">
          <Input />
        </Form.Item>
        <Form.Item label="角色" name="role" initialValue="USER">
          <Select>
            <Select.Option value="ADMIN">管理员</Select.Option>
            <Select.Option value="MANAGER">经理</Select.Option>
            <Select.Option value="USER">普通用户</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>
              {isEdit ? '保存' : '创建'}
            </Button>
            <Button onClick={() => navigate('/employees')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}