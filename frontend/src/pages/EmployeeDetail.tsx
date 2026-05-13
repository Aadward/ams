import { Descriptions, Card, Button, Space } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useEmployee } from '../api/employee';

export default function EmployeeDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: employee, isLoading } = useEmployee(Number(id));

  return (
    <Card
      loading={isLoading}
      title="员工详情"
      extra={
        <Space>
          <Button onClick={() => navigate('/employees')}>返回</Button>
        </Space>
      }
    >
      <Descriptions column={2} bordered>
        <Descriptions.Item label="姓名">{employee?.name}</Descriptions.Item>
        <Descriptions.Item label="部门">{employee?.dept ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="邮箱">{employee?.email ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="电话">{employee?.phone ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="创建时间">{employee?.createdAt}</Descriptions.Item>
        <Descriptions.Item label="更新时间">{employee?.updatedAt}</Descriptions.Item>
      </Descriptions>
    </Card>
  );
}