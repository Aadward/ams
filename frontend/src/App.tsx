import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from 'antd';
import { AuthProvider, useAuth } from './contexts/AuthContext';
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
import SupplierList from './pages/SupplierList';
import SupplierForm from './pages/SupplierForm';
import SupplierDetail from './pages/SupplierDetail';
import InventoryPlanList from './pages/InventoryPlanList';
import InventoryTaskList from './pages/InventoryTaskList';
import InventoryReport from './pages/InventoryReport';
import BorrowList from './pages/BorrowList';
import BorrowApply from './pages/BorrowApply';
import TransferList from './pages/TransferList';
import TransferApply from './pages/TransferApply';
import TransferDetail from './pages/TransferDetail';
import ProcurementList from './pages/ProcurementList';
import ProcurementApply from './pages/ProcurementApply';
import ProcurementDetail from './pages/ProcurementDetail';
import ScanPage from './pages/ScanPage';
import InsuranceList from './pages/InsuranceList';
import InsuranceForm from './pages/InsuranceForm';
import InsuranceDetail from './pages/InsuranceDetail';
import ClaimForm from './pages/ClaimForm';
import Login from './pages/Login';
import AppMenu from './components/AppMenu';
import NotificationBell from './components/NotificationBell';

const { Header, Sider, Content } = Layout;

// Protected route wrapper
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  // Wait for AuthContext to restore token from localStorage before redirecting
  if (isLoading) {
    return null;
  }

  // Only redirect if not authenticated AND loading is complete
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

function MainLayout() {
  const { userId, logout, username } = useAuth();

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
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span style={{ fontSize: 14 }}>{username}</span>
            <NotificationBell userId={userId ?? 1} />
            <a style={{ color: '#fff', fontSize: 14 }} onClick={logout}>退出</a>
          </div>
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
            <Route path="/procurements" element={<ProcurementList />} />
            <Route path="/procurements/apply" element={<ProcurementApply />} />
            <Route path="/procurements/:id" element={<ProcurementDetail />} />
            <Route path="/scan/:assetCode" element={<ScanPage />} />
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
            <Route path="/insurance" element={<InsuranceList />} />
            <Route path="/insurance/form" element={<InsuranceForm />} />
            <Route path="/insurance/:id" element={<InsuranceDetail />} />
            <Route path="/insurance/:id/edit" element={<InsuranceForm />} />
            <Route path="/insurance/:id/claim" element={<ClaimForm />} />
            <Route path="/suppliers" element={<SupplierList />} />
            <Route path="/suppliers/new" element={<SupplierForm />} />
            <Route path="/suppliers/:id" element={<SupplierDetail />} />
            <Route path="/suppliers/:id/edit" element={<SupplierForm />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        />
      </Routes>
    </AuthProvider>
  );
}
