import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from 'antd';
import AssetList from './pages/AssetList';
import AssetDetail from './pages/AssetDetail';
import AssetForm from './pages/AssetForm';
import EmployeeList from './pages/EmployeeList';
import EmployeeDetail from './pages/EmployeeDetail';
import EmployeeForm from './pages/EmployeeForm';
import DepartmentList from './pages/DepartmentList';
import Dashboard from './pages/Dashboard';
import NotificationList from './pages/NotificationList';
import ApprovalList from './pages/ApprovalList';
import AppMenu from './components/AppMenu';

const { Header, Sider, Content } = Layout;

export default function App() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={200} theme="dark" collapsible>
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 18, fontWeight: 600 }}>
          AMS 系统
        </div>
        <AppMenu />
      </Sider>
      <Layout>
        <Header style={{ display: 'flex', alignItems: 'center', color: '#fff', fontSize: 18, paddingLeft: 24 }}>
          <span>AMS 资产管理系统</span>
        </Header>
        <Content style={{ padding: '24px 48px' }}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/assets" element={<AssetList />} />
            <Route path="/assets/new" element={<AssetForm />} />
            <Route path="/assets/:id" element={<AssetDetail />} />
            <Route path="/assets/:id/edit" element={<AssetForm />} />
            <Route path="/employees" element={<EmployeeList />} />
            <Route path="/employees/new" element={<EmployeeForm />} />
            <Route path="/employees/:id" element={<EmployeeDetail />} />
            <Route path="/employees/:id/edit" element={<EmployeeForm />} />
            <Route path="/departments" element={<DepartmentList />} />
            <Route path="/notifications" element={<NotificationList />} />
            <Route path="/approvals" element={<ApprovalList />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}