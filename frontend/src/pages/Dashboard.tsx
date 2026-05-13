import { Row, Col, Card, Statistic } from 'antd';
import { useDashboardStats } from '../api/asset';

export default function Dashboard() {
  const { data, isLoading } = useDashboardStats();

  return (
    <div>
      <Row gutter={16}>
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
    </div>
  );
}
