import { useEffect, useState } from 'react';
import { Table, Tag, Card, Typography, Statistic, Row, Col } from 'antd';
import { depreciationApi } from '../api/depreciation';
import type { DepreciationRecord } from '../api/depreciation';

const { Title } = Typography;

const CATEGORY_LABELS: Record<string, string> = {
  HARDWARE: '硬件设备',
  NETWORK: '网络设备',
  PERIPHERAL: '配件耗材',
  SOFTWARE_LICENSE: '软件许可证',
};

const formatCurrency = (val: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(val);

export default function DepreciationLedger() {
  const [data, setData] = useState<DepreciationRecord[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await depreciationApi.getLedger();
      setData(res.data);
    } catch (e) {
      console.error('Failed to load depreciation ledger', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const totalOriginal = data.reduce((sum, d) => sum + d.originalValue, 0);
  const totalAccumulated = data.reduce((sum, d) => sum + d.accumulatedDepreciation, 0);
  const totalNetValue = data.reduce((sum, d) => sum + d.currentNetValue, 0);

  const columns = [
    {
      title: '资产编码',
      dataIndex: 'assetCode',
      key: 'assetCode',
      width: 120,
    },
    {
      title: '资产名称',
      dataIndex: 'assetName',
      key: 'assetName',
      width: 150,
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 100,
      render: (cat: string) => CATEGORY_LABELS[cat] || cat,
    },
    {
      title: '购置日期',
      dataIndex: 'purchaseDate',
      key: 'purchaseDate',
      width: 110,
    },
    {
      title: '原值（元）',
      dataIndex: 'originalValue',
      key: 'originalValue',
      width: 130,
      render: (v: number) => formatCurrency(v),
    },
    {
      title: '折旧年限',
      dataIndex: 'depreciationYears',
      key: 'depreciationYears',
      width: 90,
      render: (v: number) => `${v}年`,
    },
    {
      title: '年折旧额',
      dataIndex: 'annualDepreciation',
      key: 'annualDepreciation',
      width: 130,
      render: (v: number) => formatCurrency(v),
    },
    {
      title: '已提折旧',
      dataIndex: 'accumulatedDepreciation',
      key: 'accumulatedDepreciation',
      width: 130,
      render: (v: number) => formatCurrency(v),
    },
    {
      title: '账面净值',
      dataIndex: 'currentNetValue',
      key: 'currentNetValue',
      width: 130,
      render: (v: number) => formatCurrency(v),
    },
    {
      title: '已用年限',
      dataIndex: 'yearsUsed',
      key: 'yearsUsed',
      width: 90,
      render: (v: number) => `${v}年`,
    },
    {
      title: '状态',
      dataIndex: 'fullyDepreciated',
      key: 'fullyDepreciated',
      width: 100,
      render: (v: boolean) => v
        ? <Tag color="red">已提完</Tag>
        : <Tag color="green">使用中</Tag>,
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Title level={4}>折旧台账</Title>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card><Statistic title="资产总数" value={data.length} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="原值合计" value={totalOriginal} precision={2} prefix="¥" /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="累计折旧" value={totalAccumulated} precision={2} prefix="¥" /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="账面净值合计" value={totalNetValue} precision={2} prefix="¥" /></Card>
        </Col>
      </Row>
      <Card>
        <Table
          dataSource={data}
          columns={columns}
          rowKey="assetId"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>
    </div>
  );
}