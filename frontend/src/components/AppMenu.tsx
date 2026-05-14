import { Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { PieChartOutlined, DesktopOutlined, TeamOutlined, ClusterOutlined } from '@ant-design/icons';

export default function AppMenu() {
  const navigate = useNavigate();
  const location = useLocation();

  const items = [
    {
      key: '/',
      icon: <PieChartOutlined />,
      label: '仪表盘',
    },
    {
      key: '/assets',
      icon: <DesktopOutlined />,
      label: '资产管理',
      children: [
        { key: '/assets', label: '资产列表' },
        { key: '/assets/new', label: '新建资产' },
      ],
    },
    {
      key: '/employees',
      icon: <TeamOutlined />,
      label: '员工管理',
      children: [
        { key: '/employees', label: '员工列表' },
        { key: '/employees/new', label: '新建员工' },
      ],
    },
    {
      key: '/departments',
      icon: <ClusterOutlined />,
      label: '部门管理',
    },
  ];

  const selectedKey = items.find((item) => {
    if (item.key === location.pathname) return true;
    if (item.children) {
      return item.children.some((child) => child.key === location.pathname);
    }
    return false;
  })?.key as string;

  return (
    <Menu
      theme="dark"
      mode="inline"
      selectedKeys={[selectedKey || location.pathname]}
      items={items}
      onClick={({ key }) => navigate(key)}
    />
  );
}