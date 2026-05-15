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
import BackupList from './pages/BackupList';
import MaintenanceList from './pages/MaintenanceList';
import ReportList from './pages/ReportList';
import DepreciationLedger from './pages/DepreciationLedger';
import ConsumableList from './pages/ConsumableList';
import ConsumableForm from './pages/ConsumableForm';
import ConsumableStockIn from './pages/ConsumableStockIn';
import ConsumableStockOut from './pages/ConsumableStockOut';
import ConsumableReport from './pages/ConsumableReport';
import InventoryPlanList from './pages/InventoryPlanList';
import InventoryTaskList from './pages/InventoryTaskList';
import InventoryReport from './pages/InventoryReport';
import BorrowList from './pages/BorrowList';
import BorrowApply from './pages/BorrowApply';
import TransferList from './pages/TransferList';
import TransferApply from './pages/TransferApply';
import TransferDetail from './pages/TransferDetail';
import AppMenu from './components/AppMenu';
import NotificationBell from './components/NotificationBell';

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
        <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: '#fff', fontSize: 18, paddingLeft: 24, paddingRight: 24 }}>
          <span>AMS 资产管理系统</span>
          <NotificationBell userId={1} />
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
            <Route path="/borrows" element={<BorrowList />} />
            <Route path="/borrows/apply" element={<BorrowApply />} />
            <Route path="/transfers" element={<TransferList />} />
            <Route path="/transfers/apply" element={<TransferApply />} />
            <Route path="/transfers/:id" element={<TransferDetail />} />
            <Route path="/maintenance" element={<MaintenanceList />} />
            <Route path="/backup" element={<BackupList />} />
            <Route path="/reports" element={<ReportList />} />
            <Route path="/depreciation" element={<DepreciationLedger />} />
            <Route path="/consumables" element={<ConsumableList />} />
            <Route path="/consumables/new" element={<ConsumableForm />} />
            <Route path="/consumables/:id/edit" element={<ConsumableForm />} />
            <Route path="/consumables/stock-in" element={<ConsumableStockIn />} />
            <Route path="/consumables/stock-out" element={<ConsumableStockOut />} />
            <Route path="/consumables/report" element={<ConsumableReport />} />
            <Route path="/inventory-plan" element={<InventoryPlanList />} />
            <Route path="/inventory-task" element={<InventoryTaskList />} />
            <Route path="/inventory-report/:planId" element={<InventoryReport />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}