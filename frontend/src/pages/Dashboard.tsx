import { Row, Col, Card, Statistic, Progress, Tag, Segmented } from 'antd';
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDashboardStats, useExpiringWarranty, useExpiringInsurance } from '../api/asset';
import { approvalApi } from '../api/approval';
import { useAuth } from '../contexts/AuthContext';
import type { ApprovalRequest } from '../api/approval';

export default function Dashboard() {
  const { data, isLoading } = useDashboardStats();
  const [warrantyDays, setWarrantyDays] = useState<number>(30);
  const { data: expiringAssets } = useExpiringWarranty(warrantyDays);
  const [insuranceDays, setInsuranceDays] = useState<number>(30);
  const { data: expiringInsurances } = useExpiringInsurance(insuranceDays);
  const { userId, role } = useAuth();

  const [myAssetCount, setMyAssetCount] = useState<number>(0);
  const [myRequests, setMyRequests] = useState<ApprovalRequest[]>([]);
  const [pendingCount, setPendingCount] = useState<number>(0);

  useEffect(() => {
    if (userId) {
      approvalApi.getMyAssets().then(res => {
        setMyAssetCount(res.data.length);
      }).catch(() => {});
    }
  }, [userId]);

  useEffect(() => {
    if (userId) {
      approvalApi.listMy(userId).then(res => {
        setMyRequests(res.data.slice(0, 5));
      }).catch(() => {});
    }
  }, [userId]);

  useEffect(() => {
    if (role === 'ADMIN' || role === 'MANAGER') {
      approvalApi.getPendingCount().then(res => {
        setPendingCount(res.data.count);
      }).catch(() => {});
    }
  }, [role]);

  const categoryLabels: Record<string, string> = {
    HARDWARE: '硬件设备',
    NETWORK: '网络设备',
    PERIPHERAL: '配件耗材',
    SOFTWARE_LICENSE: '软件许可证',
  };

  const totalCategoryCount = data?.categoryStats
    ? Object.values(data.categoryStats).reduce((sum, val) => sum + val, 0)
    : 0;

  const maxTrend = data?.monthlyTrend && data.monthlyTrend.length > 0
    ? Math.max(...data.monthlyTrend.map(m => m.count))
    : 1;

  const getDaysRemaining = (warrantyEnd: string) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const end = new Date(warrantyEnd);
    end.setHours(0, 0, 0, 0);
    const diff = Math.ceil((end.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  const getDaysTagColor = (days: number) => {
    if (days <= 7) return 'red';
    if (days <= 15) return 'orange';
    if (days <= 30) return 'gold';
    return 'green';
  };

  return (
    <div>
      <h2>仪表盘</h2>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card loading={isLoading}>
            <Statistic title="资产总数" value={data?.totalAssets ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={isLoading}>
            <Statistic title="库存" value={data?.inStock ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={isLoading}>
            <Statistic title="已领用" value={data?.inUse ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={isLoading}>
            <Statistic title="维修中" value={data?.inMaintenance ?? 0} />
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card loading={isLoading}>
            <Statistic title="已报废" value={data?.retired ?? 0} />
          </Card>
        </Col>
      </Row>
      <Row gutter={16}>
        <Col span={12}>
          <Card title="分类统计" loading={isLoading}>
            {data?.categoryStats && totalCategoryCount > 0 ? (
              Object.entries(data.categoryStats).map(([key, value]) => (
                <div key={key} style={{ marginBottom: 8 }}>
                  <Tag color="blue">{categoryLabels[key] || key}</Tag>
                  <Progress
                    percent={Math.round((value / totalCategoryCount) * 100)}
                    size="small"
                    format={() => `${value}`}
                  />
                </div>
              ))
            ) : (
              <span>暂无数据</span>
            )}
          </Card>
        </Col>
        <Col span={12}>
          <Card title="月度活动趋势" loading={isLoading}>
            {data?.monthlyTrend && data.monthlyTrend.length > 0 ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {data.monthlyTrend.map((entry) => (
                  <div key={entry.month} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ width: 60, fontSize: 12, color: '#666' }}>{entry.month}</span>
                    <div style={{ flex: 1, background: '#f0f0f0', borderRadius: 4, height: 20 }}>
                      <div
                        style={{
                          width: `${Math.max(2, (entry.count / maxTrend) * 100)}%`,
                          background: '#1677ff',
                          borderRadius: 4,
                          height: '100%',
                          display: 'flex',
                          alignItems: 'center',
                          paddingLeft: 6,
                          color: '#fff',
                          fontSize: 11,
                        }}
                      >
                        {entry.count}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div>暂无数据</div>
            )}
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={24}>
          <Card
            title={
              <span>
                维保即将到期
                {expiringAssets && expiringAssets.length > 0 && (
                  <Tag color="red" style={{ marginLeft: 8 }}>{expiringAssets.length}</Tag>
                )}
              </span>
            }
            extra={
              <Segmented
                value={warrantyDays}
                onChange={(val) => setWarrantyDays(val as number)}
                options={[
                  { label: '7天', value: 7 },
                  { label: '15天', value: 15 },
                  { label: '30天', value: 30 },
                  { label: '60天', value: 60 },
                ]}
              />
            }
            loading={isLoading}
          >
            {expiringAssets && expiringAssets.length > 0 ? (
              <>
                <div style={{ maxHeight: 240, overflow: 'auto' }}>
                  {expiringAssets.slice(0, 5).map((asset) => {
                    const daysRemaining = getDaysRemaining(asset.warrantyEnd);
                    return (
                      <div
                        key={asset.id}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          padding: '8px 0',
                          borderBottom: '1px solid #f0f0f0',
                        }}
                      >
                        <div>
                          <div style={{ fontWeight: 500 }}>{asset.name}</div>
                          <div style={{ fontSize: 12, color: '#666' }}>
                            {asset.assetCode} · {categoryLabels[asset.category] || asset.category}
                          </div>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ fontSize: 12, color: '#666' }}>到期：{asset.warrantyEnd}</div>
                          <Tag color={getDaysTagColor(daysRemaining)} style={{ marginTop: 2 }}>
                            剩余{daysRemaining}天
                          </Tag>
                        </div>
                      </div>
                    );
                  })}
                </div>
                {expiringAssets.length > 5 && (
                  <div style={{ marginTop: 8, textAlign: 'center', color: '#666' }}>
                    还有 {expiringAssets.length - 5} 条...
                  </div>
                )}
              </>
            ) : (
              <div style={{ textAlign: 'center', color: '#666', padding: '24px 0' }}>
                暂无即将到期的维保资产
              </div>
            )}
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={24}>
          <Card
            title={
              <span>
                保险即将到期
                {expiringInsurances && expiringInsurances.length > 0 && (
                  <Tag color="orange" style={{ marginLeft: 8 }}>{expiringInsurances.length}</Tag>
                )}
              </span>
            }
            extra={
              <Segmented
                value={insuranceDays}
                onChange={(val) => setInsuranceDays(val as number)}
                options={[
                  { label: '7天', value: 7 },
                  { label: '15天', value: 15 },
                  { label: '30天', value: 30 },
                  { label: '60天', value: 60 },
                ]}
              />
            }
            loading={isLoading}
          >
            {expiringInsurances && expiringInsurances.length > 0 ? (
              <>
                <div style={{ maxHeight: 240, overflow: 'auto' }}>
                  {expiringInsurances.slice(0, 5).map((insurance) => {
                    const daysRemaining = getDaysRemaining(insurance.endDate);
                    return (
                      <div
                        key={insurance.id}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          padding: '8px 0',
                          borderBottom: '1px solid #f0f0f0',
                        }}
                      >
                        <div>
                          <div style={{ fontWeight: 500 }}>{insurance.assetName}</div>
                          <div style={{ fontSize: 12, color: '#666' }}>
                            {insurance.assetCode} · {insurance.insuranceCompany} · {insurance.policyNumber}
                          </div>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ fontSize: 12, color: '#666' }}>到期：{insurance.endDate}</div>
                          <Tag color={getDaysTagColor(daysRemaining)} style={{ marginTop: 2 }}>
                            剩余{daysRemaining}天
                          </Tag>
                        </div>
                      </div>
                    );
                  })}
                </div>
                {expiringInsurances.length > 5 && (
                  <div style={{ marginTop: 8, textAlign: 'center', color: '#666' }}>
                    还有 {expiringInsurances.length - 5} 条...
                  </div>
                )}
              </>
            ) : (
              <div style={{ textAlign: 'center', color: '#666', padding: '24px 0' }}>
                暂无即将到期的保险
              </div>
            )}
          </Card>
        </Col>
      </Row>
      <h3 style={{ marginTop: 24, marginBottom: 16 }}>我的</h3>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic title="我的资产" value={myAssetCount} />
            <Link to="/assets" style={{ fontSize: 12 }}>查看全部 →</Link>
          </Card>
        </Col>
        <Col span={8}>
          <Card title="我的申请">
            {myRequests.length > 0 ? (
              <div style={{ maxHeight: 150, overflow: 'auto' }}>
                {myRequests.map(req => (
                  <div key={req.id} style={{ padding: '4px 0', borderBottom: '1px solid #f0f0f0' }}>
                    <div style={{ fontSize: 12 }}>{req.assetName || '资产#' + req.assetId}</div>
                    <Tag color={req.status === 'PENDING' ? 'gold' : req.status === 'APPROVED' ? 'green' : 'red'}>
                      {req.status === 'PENDING' ? '待审批' : req.status === 'APPROVED' ? '已通过' : '已拒绝'}
                    </Tag>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ color: '#666', fontSize: 12 }}>暂无申请</div>
            )}
            <Link to="/approvals" style={{ fontSize: 12 }}>查看全部 →</Link>
          </Card>
        </Col>
        {(role === 'ADMIN' || role === 'MANAGER') && (
          <Col span={8}>
            <Card>
              <Statistic title="我的待审批" value={pendingCount} />
              <Link to="/approvals" style={{ fontSize: 12 }}>查看全部 →</Link>
            </Card>
          </Col>
        )}
      </Row>
    </div>
  );
}