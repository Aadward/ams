import { Table, Button, Space } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useEmployeeList } from '../api/employee';

export default function EmployeeList() {
  const navigate = useNavigate();
  const { data, isLoading } = useEmployeeList();

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