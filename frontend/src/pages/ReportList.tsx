import { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Table, Tag, Space } from 'antd';
import {
  PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from 'recharts';
import { reportApi, AssetStatisticsResponse, MaintenanceCostSummary } from '../api/report';

const { Column } = Table;

const CATEGORY_LABELS: Record<string, string> = {
  HARDWARE: '硬件设备',
  NETWORK: '网络设备',
  PERIPHERAL: '配件耗材',
  SOFTWARE_LICENSE: '软件许可证',
};

const STATUS_LABELS: Record<string, string> = {
  IN_STOCK: '库存',
  IN_USE: '已领用',
  MAINTENANCE: '维修中',
  RETIRED: '已报废',
};

const STATUS_COLORS: Record<string, string> = {
  IN_STOCK: '#52c41a',
  IN_USE: '#1677ff',
  MAINTENANCE: '#faad14',
  RETIRED: '#ff4d4f',
};

const CATEGORY_COLORS = ['#1677ff', '#52c41a', '#faad14', '#722ed1'];

const formatCurrency = (val: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(val);

export default function ReportList() {
  const [categoryData, setCategoryData] = useState<AssetStatisticsResponse[]>([]);
  const [statusData, setStatusData] = useState<AssetStatisticsResponse[]>([]);
  const [deptData, setDeptData] = useState<AssetStatisticsResponse[]>([]);
  const [costData, setCostData] = useState<MaintenanceCostSummary | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [cat, stat, dept, cost] = await Promise.all([
        reportApi.byCategory().then(r => r.data),
        reportApi.byStatus().then(r => r.data),
        reportApi.byDepartment().then(r => r.data),
        reportApi.maintenanceCost().then(r => r.data),
      ]);
      setCategoryData(cat);
      setStatusData(stat);
      setDeptData(dept);
      setCostData(cost);
    } catch (e) {
      console.error('Failed to load reports', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
  }, []);

  const totalAssets = statusData.reduce((sum, d) => sum + d.count, 0);

  const categoryChartData = categoryData.map(d => ({
    name: CATEGORY_LABELS[d.name] || d.name,
    value: d.count,
    totalValue: d.totalValue,
  }));

  const statusChartData = statusData.map(d => ({
    name: STATUS_LABELS[d.name] || d.name,
    value: d.count,
  }));

  const deptChartData = deptData.map(d => ({
    name: d.name,
    value: d.count,
    totalValue: d.totalValue,
  }));

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <h2>资产统计报表</h2>

      <Row gutter={16}>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic title="资产总数" value={totalAssets} />
          </Card>
        </Col>
        {costData && (
          <>
            <Col span={6}>
              <Card loading={loading}>
                <Statistic
                  title="本月维修费用"
                  value={costData.monthlyCost}
                  precision={2}
                  prefix="¥"
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card loading={loading}>
                <Statistic
                  title="本季维修费用"
                  value={costData.quarterlyCost}
                  precision={2}
                  prefix="¥"
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card loading={loading}>
                <Statistic
                  title="本年维修费用"
                  value={costData.yearlyCost}
                  precision={2}
                  prefix="¥"
                />
              </Card>
            </Col>
          </>
        )}
      </Row>

      <Row gutter={16}>
        <Col span={12}>
          <Card title="资产分类分布" loading={loading}>
            {categoryChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={categoryChartData}
                    cx="50%"
                    cy="50%"
                    outerRadius={100}
                    dataKey="value"
                    label={({ name, percent }) =>
                      `${name} ${((percent ?? 0) * 100).toFixed(0)}%`
                    }
                  >
                    {categoryChartData.map((_, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={CATEGORY_COLORS[index % CATEGORY_COLORS.length]}
                      />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(value, name, props) => {
                      const p = props.payload as { totalValue?: number };
                      return [`${value} 件 / ${formatCurrency(p?.totalValue ?? 0)}`, String(name)];
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <span>暂无数据</span>
            )}
          </Card>
        </Col>

        <Col span={12}>
          <Card title="资产状态分布" loading={loading}>
            {statusChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={statusChartData}
                    cx="50%"
                    cy="50%"
                    outerRadius={100}
                    dataKey="value"
                    label={({ name, percent }) =>
                      `${name} ${((percent ?? 0) * 100).toFixed(0)}%`
                    }
                  >
                    {statusChartData.map((entry) => (
                      <Cell
                        key={`cell-${entry.name}`}
                        fill={STATUS_COLORS[entry.name] || '#999'}
                      />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <span>暂无数据</span>
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={16}>
          <Card title="部门资产分布" loading={loading}>
            {deptChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={deptChartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip
                    formatter={(value, name, props) => {
                      const p = props.payload as { totalValue?: number };
                      return [`${value} 件 / ${formatCurrency(p?.totalValue ?? 0)}`, String(name)];
                    }}
                  />
                  <Legend />
                  <Bar dataKey="value" name="资产数量" fill="#1677ff" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <span>暂无数据</span>
            )}
          </Card>
        </Col>

        <Col span={8}>
          <Card title="分类明细" loading={loading}>
            <Table
              dataSource={categoryData}
              rowKey="name"
              pagination={false}
              size="small"
            >
              <Column
                title="分类"
                dataIndex="name"
                render={(v: string) => (
                  <Tag>{CATEGORY_LABELS[v] || v}</Tag>
                )}
              />
              <Column title="数量" dataIndex="count" />
              <Column
                title="总价值"
                dataIndex="totalValue"
                render={(v: number) => formatCurrency(v)}
              />
            </Table>
          </Card>
        </Col>
      </Row>

      {costData && (
        <Row gutter={16}>
          <Col span={24}>
            <Card title="维修费用统计" loading={loading}>
              <Table
                dataSource={[
                  { period: '本月', count: costData.monthlyCount, cost: costData.monthlyCost },
                  { period: '本季', count: costData.quarterlyCount, cost: costData.quarterlyCost },
                  { period: '本年', count: costData.yearlyCount, cost: costData.yearlyCost },
                ]}
                rowKey="period"
                pagination={false}
                size="small"
              >
                <Column title="周期" dataIndex="period" />
                <Column title="维修次数" dataIndex="count" />
                <Column
                  title="总费用"
                  dataIndex="cost"
                  render={(v: number) => (
                    <span style={{ color: '#fa8c16', fontWeight: 600 }}>
                      {formatCurrency(v)}
                    </span>
                  )}
                />
              </Table>
            </Card>
          </Col>
        </Row>
      )}
    </Space>
  );
}
