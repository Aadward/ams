import { Row, Col, Card, Statistic, Progress, Tag } from 'antd';
import { useDashboardStats } from '../api/asset';

export default function Dashboard() {
  const { data, isLoading } = useDashboardStats();

  const categoryLabels: Record<string, string> = {
    HARDWARE: '硬件设备',
    NETWORK: '网络设备',
    PERIPHERAL: '配件耗材',
    SOFTWARE_LICENSE: '软件许可证',
  };

  const totalCategoryCount = data?.categoryStats
    ? Object.values(data.categoryStats).reduce((sum, val) => sum + val, 0)
    : 0;

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
        <Col span={24}>
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
      </Row>
    </div>
  );
}