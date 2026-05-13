import { Descriptions, Card, Button, Space } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';

export default function EmployeeDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  return (
    <Card title="员工详情" extra={
      <Space>
        <Button>编辑</Button>
        <Button onClick={() => navigate('/employees')}>返回</Button>
      </Space>
    }>
      <Descriptions column={2} bordered>
        <Descriptions.Item label="姓名">-</Descriptions.Item>
        <Descriptions.Item label="部门">-</Descriptions.Item>
        <Descriptions.Item label="邮箱">-</Descriptions.Item>
        <Descriptions.Item label="电话">-</Descriptions.Item>
      </Descriptions>
    </Card>
  );
}
