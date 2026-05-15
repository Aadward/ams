import { Card, Button, Result, Spin, Descriptions, Modal, Input, message } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useScanAsset, useScanAssign, useScanReturn, useScanBorrowReturn, ScanResponse } from '../api/scan';
import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons';

const { TextArea } = Input;

const statusMap: Record<string, { color: string; label: string }> = {
  IN_STOCK: { color: 'green', label: '在库' },
  IN_USE: { color: 'blue', label: '在用' },
  MAINTENANCE: { color: 'orange', label: '维修中' },
  RETIRED: { color: 'red', label: '已报废' },
};

const categoryLabel: Record<string, string> = {
  HARDWARE: '硬件设备',
  NETWORK: '网络设备',
  PERIPHERAL: '配件耗材',
  SOFTWARE_LICENSE: '软件许可证',
};

const getCurrentEmployeeId = (): number | null => {
  const stored = localStorage.getItem('currentEmployeeId');
  return stored ? parseInt(stored, 10) : null;
};

export default function ScanPage() {
  const { assetCode } = useParams<{ assetCode: string }>();
  const navigate = useNavigate();
  const [assetData, setAssetData] = useState<ScanResponse | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [reasonModalVisible, setReasonModalVisible] = useState(false);
  const [pendingAction, setPendingAction] = useState<'ASSIGN' | 'RETURN' | null>(null);
  const [reason, setReason] = useState('');

  const employeeId = getCurrentEmployeeId();

  const scanMutation = useScanAsset();
  const assignMutation = useScanAssign();
  const returnMutation = useScanReturn();
  const borrowReturnMutation = useScanBorrowReturn();

  useEffect(() => {
    if (assetCode && employeeId) {
      scanMutation.mutate(
        { assetCode, employeeId },
        {
          onSuccess: (data) => {
            setAssetData(data);
            setErrorMsg(null);
          },
          onError: (err: unknown) => {
            const error = err as { response?: { data?: { message?: string } }; message?: string };
            setErrorMsg(error.response?.data?.message || error.message || '加载失败');
            setAssetData(null);
          },
        }
      );
    }
  }, [assetCode, employeeId]);

  const handleAction = (action: 'ASSIGN' | 'RETURN' | 'BORROW_RETURN') => {
    if (!assetCode || !employeeId) return;

    if (action === 'BORROW_RETURN') {
      borrowReturnMutation.mutate(
        { assetCode, employeeId },
        {
          onSuccess: () => {
            setActionSuccess('BORROW_RETURN');
          },
          onError: (err: unknown) => {
            const error = err as { response?: { data?: { message?: string } }; message?: string };
            message.error(error.response?.data?.message || error.message || '操作失败');
          },
        }
      );
    } else {
      setPendingAction(action);
      setReason('');
      setReasonModalVisible(true);
    }
  };

  const handleReasonConfirm = () => {
    if (!assetCode || !employeeId || !pendingAction) return;

    if (pendingAction === 'ASSIGN') {
      assignMutation.mutate(
        { assetCode, reason, employeeId },
        {
          onSuccess: () => {
            setActionSuccess('ASSIGN');
            setReasonModalVisible(false);
          },
          onError: (err: unknown) => {
            const error = err as { response?: { data?: { message?: string } }; message?: string };
            message.error(error.response?.data?.message || error.message || '操作失败');
            setReasonModalVisible(false);
          },
        }
      );
    } else if (pendingAction === 'RETURN') {
      returnMutation.mutate(
        { assetCode, reason, employeeId },
        {
          onSuccess: () => {
            setActionSuccess('RETURN');
            setReasonModalVisible(false);
          },
          onError: (err: unknown) => {
            const error = err as { response?: { data?: { message?: string } }; message?: string };
            message.error(error.response?.data?.message || error.message || '操作失败');
            setReasonModalVisible(false);
          },
        }
      );
    }
  };

  const handleViewDetail = () => {
    if (assetData?.assetId) {
      navigate(`/assets/${assetData.assetId}`);
    }
  };

  const handleBack = () => {
    navigate(-1);
  };

  // Success result view
  if (actionSuccess) {
    const actionLabels: Record<string, string> = {
      ASSIGN: '领用成功',
      RETURN: '归还成功',
      BORROW_RETURN: '借用归还成功',
    };

    return (
      <div style={{ padding: '16px', maxWidth: '480px', margin: '0 auto' }}>
        <Result
          icon={<CheckCircleFilled style={{ color: '#52c41a' }} />}
          title={actionLabels[actionSuccess] || '操作成功'}
          subTitle={`资产: ${assetData?.assetName || assetCode}`}
          extra={
            <Button type="primary" onClick={handleBack}>
              返回
            </Button>
          }
        />
      </div>
    );
  }

  // Loading state
  if (scanMutation.isPending) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Spin size="large" tip="加载资产信息..." />
      </div>
    );
  }

  // Error state
  if (errorMsg) {
    return (
      <div style={{ padding: '16px', maxWidth: '480px', margin: '0 auto' }}>
        <Result
          icon={<CloseCircleFilled style={{ color: '#ff4d4f' }} />}
          title="加载失败"
          subTitle={errorMsg}
          extra={
            <Button type="primary" onClick={handleBack}>
              返回
            </Button>
          }
        />
      </div>
    );
  }

  // No data
  if (!assetData) {
    return (
      <div style={{ padding: '16px', maxWidth: '480px', margin: '0 auto' }}>
        <Result
          title="未找到资产信息"
          extra={
            <Button type="primary" onClick={handleBack}>
              返回
            </Button>
          }
        />
      </div>
    );
  }

  const statusInfo = statusMap[assetData.status] || { color: 'default', label: assetData.status };

  return (
    <div style={{ padding: '16px', maxWidth: '480px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ marginBottom: '16px' }}>
        <Button onClick={handleBack} style={{ marginBottom: '8px' }}>
          返回
        </Button>
        <h2 style={{ margin: '8px 0', fontSize: '18px', fontWeight: 600 }}>资产扫描</h2>
      </div>

      {/* Asset Info Card */}
      <Card title="资产信息" style={{ marginBottom: '16px' }}>
        <Descriptions column={1} size="small">
          <Descriptions.Item label="资产名称">{assetData.assetName}</Descriptions.Item>
          <Descriptions.Item label="资产编码">{assetData.assetCode}</Descriptions.Item>
          <Descriptions.Item label="资产分类">
            {categoryLabel[assetData.category] || assetData.category}
          </Descriptions.Item>
          <Descriptions.Item label="资产状态">
            <span style={{ color: statusInfo.color, fontWeight: 500 }}>{statusInfo.label}</span>
          </Descriptions.Item>
          <Descriptions.Item label="存放地点">{assetData.location || '-'}</Descriptions.Item>
          <Descriptions.Item label="当前使用人">{assetData.assigneeName || '-'}</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* Action Buttons */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {assetData.availableActions.includes('ASSIGN') && (
          <Button
            type="primary"
            size="large"
            block
            onClick={() => handleAction('ASSIGN')}
            loading={assignMutation.isPending}
          >
            领用
          </Button>
        )}

        {assetData.availableActions.includes('RETURN') && (
          <Button
            size="large"
            block
            onClick={() => handleAction('RETURN')}
            loading={returnMutation.isPending}
          >
            归还
          </Button>
        )}

        {assetData.availableActions.includes('BORROW_RETURN') && (
          <Button
            size="large"
            block
            onClick={() => handleAction('BORROW_RETURN')}
            loading={borrowReturnMutation.isPending}
          >
            借用归还
          </Button>
        )}

        {assetData.availableActions.includes('VIEW_DETAIL') && (
          <Button size="large" block onClick={handleViewDetail}>
            查看详情
          </Button>
        )}
      </div>

      {/* Reason Modal */}
      <Modal
        title={pendingAction === 'ASSIGN' ? '领用原因' : '归还原因'}
        open={reasonModalVisible}
        onOk={handleReasonConfirm}
        onCancel={() => setReasonModalVisible(false)}
        okText="确认"
        cancelText="取消"
        confirmLoading={assignMutation.isPending || returnMutation.isPending}
      >
        <div style={{ marginBottom: '8px' }}>请输入操作原因：</div>
        <TextArea
          rows={3}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          placeholder="请输入原因..."
        />
      </Modal>
    </div>
  );
}
