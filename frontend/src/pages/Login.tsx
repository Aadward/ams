import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { authApi } from '../api/auth';
import { useAuth } from '../contexts/AuthContext';

interface LoginFormValues {
  username: string;
  password: string;
}

export default function Login() {
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  const onFinish = async (values: LoginFormValues) => {
    setLoading(true);
    try {
      const res = await authApi.login(values);
      const { token, userId, username, role } = res.data;

      // Update AuthContext state (which also persists to localStorage)
      login(token, userId, username, role);

      // Navigate to home (use hash routing)
      window.location.hash = '#/';
    } catch {
      message.error('用户名或密码错误');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    }}>
      <Card
        title={<span style={{ fontSize: 20, fontWeight: 600 }}>AMS 资产管理系统</span>}
        style={{ width: 380 }}
        bordered
      >
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名: admin"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码: admin123"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block size="large">
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
