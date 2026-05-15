import { Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { PieChartOutlined, DesktopOutlined, TeamOutlined, ClusterOutlined, BellOutlined, FileTextOutlined, CloudUploadOutlined, ToolOutlined, BarChartOutlined, InboxOutlined, AuditOutlined, SwapOutlined, ArrowsAltOutlined } from '@ant-design/icons';

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
    {
      key: '/approvals',
      icon: <FileTextOutlined />,
      label: '审批请求',
    },
    {
      key: '/borrows',
      icon: <SwapOutlined />,
      label: '借用管理',
      children: [
        { key: '/borrows', label: '借用记录' },
        { key: '/borrows/apply', label: '申请借用' },
      ],
    },
    {
      key: '/transfers',
      icon: <ArrowsAltOutlined />,
      label: '调拨管理',
      children: [
        { key: '/transfers', label: '调拨记录' },
        { key: '/transfers/apply', label: '申请调拨' },
      ],
    },
    {
      key: '/maintenance',
      icon: <ToolOutlined />,
      label: '维修记录',
    },
    {
      key: '/notifications',
      icon: <BellOutlined />,
      label: '通知中心',
    },
    {
      key: '/backup',
      icon: <CloudUploadOutlined />,
      label: '备份管理',
    },
    {
      key: '/reports',
      icon: <BarChartOutlined />,
      label: '统计报表',
    },
    {
      key: '/depreciation',
      label: '折旧台账',
    },
    {
      key: '/consumables',
      icon: <InboxOutlined />,
      label: '低值易耗品',
      children: [
        { key: '/consumables', label: '易耗品管理' },
        { key: '/consumables/stock-in', label: '入库管理' },
        { key: '/consumables/stock-out', label: '出库管理' },
        { key: '/consumables/report', label: '消耗报表' },
      ],
    },
    {
      key: '/inventory-plan',
      icon: <AuditOutlined />,
      label: '定期盘点',
      children: [
        { key: '/inventory-plan', label: '盘点计划' },
        { key: '/inventory-task', label: '盘点任务' },
      ],
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