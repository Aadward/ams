import { Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';

export default function AppMenu() {
  const navigate = useNavigate();
  const location = useLocation();

  const items = [
    { key: '/', label: '仪表盘' },
    { key: '/assets', label: '资产管理' },
    { key: '/employees', label: '员工管理' },
  ];

  return (
    <Menu
      theme="dark"
      mode="horizontal"
      selectedKeys={[location.pathname]}
      items={items}
      onClick={({ key }) => navigate(key)}
    />
  );
}
