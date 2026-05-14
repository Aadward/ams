import { Table, Button, Space, Select, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useEmployeeList, useUpdateEmployeeRole } from '../api/employee';

export default function EmployeeList() {
  const navigate = useNavigate();
  const { data, isLoading } = useEmployeeList();
  const updateRoleMutation = useUpdateEmployeeRole();

  const handleRoleChange = async (employeeId: number, newRole: string) => {
    try {
      await updateRoleMutation.mutateAsync({ id: employeeId, role: newRole });
      message.success('角色更新成功');
    } catch {
      message.error('角色更新失败');
    }
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Space>
        <Button type="primary" onClick={() => navigate('/employees/new')}>新建员工</Button>
      </Space>
      <Table
        loading={isLoading}
        dataSource={data?.content}
        rowKey="id"
        pagination={{ total: data?.totalElements, defaultPageSize: 10 }}
        onRow={(record) => ({
          onClick: () => navigate(`/employees/${record.id}`),
          style: { cursor: 'pointer' },
        })}
        columns={[
          { title: '姓名', dataIndex: 'name' },
          { title: '部门', dataIndex: 'deptName' },
          { title: '邮箱', dataIndex: 'email' },
          { title: '电话', dataIndex: 'phone' },
          {
            title: '角色',
            dataIndex: 'role',
            render: (role: string, record: { id: number }) => (
              <Select
                value={role}
                size="small"
                style={{ width: 100 }}
                onClick={(e) => e.stopPropagation()}
                onChange={(val) => handleRoleChange(record.id, val)}
                options={[
                  { value: 'ADMIN', label: '管理员' },
                  { value: 'MANAGER', label: '经理' },
                  { value: 'USER', label: '普通用户' },
                ]}
              />
            ),
          },
          {
            title: '操作',
            render: (_: unknown, record: { id: number }) => (
              <Button size="small" onClick={(e) => { e.stopPropagation(); navigate(`/employees/${record.id}`); }}>
                查看
              </Button>
            ),
          },
        ]}
      />
    </Space>
  );
}
