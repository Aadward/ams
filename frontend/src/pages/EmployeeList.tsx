import { Table, Button, Space, Input } from 'antd';
import { useNavigate } from 'react-router-dom';

export default function EmployeeList() {
  const navigate = useNavigate();

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Space>
        <Input.Search placeholder="搜索姓名/部门" style={{ width: 240 }} />
        <Button type="primary" onClick={() => navigate('/employees/new')}>新建员工</Button>
      </Space>
      <Table
        rowKey="id"
        onRow={(record) => ({
          onClick: () => navigate(`/employees/${record.id}`),
          style: { cursor: 'pointer' },
        })}
        columns={[
          { title: '姓名', dataIndex: 'name' },
          { title: '部门', dataIndex: 'dept' },
          { title: '邮箱', dataIndex: 'email' },
          { title: '电话', dataIndex: 'phone' },
        ]}
      />
    </Space>
  );
}
