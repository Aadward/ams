import { useEffect, useState } from 'react';
import { Table, Tag, Card, Typography, Statistic, Row, Col, Tabs } from 'antd';
import { depreciationApi, DepreciationSummary } from '../api/depreciation';
import type { DepreciationRecord } from '../api/report';

const { Title } = Typography;

const CATEGORY_LABELS: Record<string, string> = {
  HARDWARE: '硬件设备',
  NETWORK: '网络设备',
  PERIPHERAL: '配件耗材',
  SOFTWARE_LICENSE: '软件许可证',
};

const formatCurrency = (val: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(val);

const formatRate = (val: number) => `${(val * 100).toFixed(2)}%`;

export default function DepreciationLedger() {
  const [data, setData] = useState<DepreciationRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [categorySummary, setCategorySummary] = useState<DepreciationSummary[]>([]);
  const [deptSummary, setDeptSummary] = useState<DepreciationSummary[]>([]);
  const [summaryLoading, setSummaryLoading] = useState(true);

  const fetchLedger = async () => {
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

  const fetchSummaries = async () => {
    setSummaryLoading(true);
    try {
      const [catRes, deptRes] = await Promise.all([
        depreciationApi.getSummaryByCategory(),
        depreciationApi.getSummaryByDepartment(),
      ]);
      setCategorySummary(catRes.data);
      setDeptSummary(deptRes.data);
    } catch (e) {
      console.error('Failed to load summaries', e);
    } finally {
      setSummaryLoading(false);
    }
  };

  useEffect(() => {
    fetchLedger();
    fetchSummaries();
  }, []);

  const totalOriginal = data.reduce((sum, d) => sum + (d.originalValue || 0), 0);
  const totalAccumulated = data.reduce((sum, d) => sum + (d.accumulatedDepreciation || 0), 0);
  const totalNetValue = data.reduce((sum, d) => sum + (d.currentNetValue || 0), 0);

  const ledgerColumns = [
    { title: '资产编码', dataIndex: 'assetCode', key: 'assetCode', width: 120 },
    { title: '资产名称', dataIndex: 'assetName', key: 'assetName', width: 150 },
    { title: '分类', dataIndex: 'category', key: 'category', width: 100, render: (cat: string) => CATEGORY_LABELS[cat] || cat },
    { title: '购置日期', dataIndex: 'purchaseDate', key: 'purchaseDate', width: 110 },
    { title: '原值（元）', dataIndex: 'originalValue', key: 'originalValue', width: 130, render: (v: number) => formatCurrency(v) },
    { title: '折旧年限', dataIndex: 'depreciationYears', key: 'depreciationYears', width: 90, render: (v: number) => `${v}年` },
    { title: '年折旧额', dataIndex: 'annualDepreciation', key: 'annualDepreciation', width: 130, render: (v: number) => formatCurrency(v) },
    { title: '已提折旧', dataIndex: 'accumulatedDepreciation', key: 'accumulatedDepreciation', width: 130, render: (v: number) => formatCurrency(v) },
    { title: '账面净值', dataIndex: 'currentNetValue', key: 'currentNetValue', width: 130, render: (v: number) => formatCurrency(v) },
    { title: '已用年限', dataIndex: 'yearsUsed', key: 'yearsUsed', width: 90, render: (v: number) => `${v}年` },
    { title: '状态', dataIndex: 'fullyDepreciated', key: 'fullyDepreciated', width: 100, render: (v: boolean) => v ? <Tag color="red">已提完</Tag> : <Tag color="green">使用中</Tag> },
  ];

  const summaryColumns = [
    { title: '分组', dataIndex: 'groupLabel', key: 'groupLabel', width: 150 },
    { title: '资产数量', dataIndex: 'assetCount', key: 'assetCount', width: 100 },
    { title: '原值合计', dataIndex: 'totalOriginalValue', key: 'totalOriginalValue', width: 150, render: (v: number) => formatCurrency(v) },
    { title: '累计折旧', dataIndex: 'totalAccumulatedDepreciation', key: 'totalAccumulatedDepreciation', width: 150, render: (v: number) => formatCurrency(v) },
    { title: '账面净值', dataIndex: 'totalNetValue', key: 'totalNetValue', width: 150, render: (v: number) => formatCurrency(v) },
    { title: '折旧率', dataIndex: 'depreciationRate', key: 'depreciationRate', width: 100, render: (v: number) => formatRate(v) },
  ];

  const catTotals = {
    count: categorySummary.reduce((s, d) => s + d.assetCount, 0),
    original: categorySummary.reduce((s, d) => s + d.totalOriginalValue, 0),
    accumulated: categorySummary.reduce((s, d) => s + d.totalAccumulatedDepreciation, 0),
    net: categorySummary.reduce((s, d) => s + d.totalNetValue, 0),
  };

  const deptTotals = {
    count: deptSummary.reduce((s, d) => s + d.assetCount, 0),
    original: deptSummary.reduce((s, d) => s + d.totalOriginalValue, 0),
    accumulated: deptSummary.reduce((s, d) => s + d.totalAccumulatedDepreciation, 0),
    net: deptSummary.reduce((s, d) => s + d.totalNetValue, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Title level={4}>折旧管理</Title>
      <Tabs
        items={[
          {
            key: 'ledger',
            label: '折旧台账',
            children: (
              <>
                <Row gutter={16} style={{ marginBottom: 24 }}>
                  <Col span={6}><Card><Statistic title="资产总数" value={data.length} /></Card></Col>
                  <Col span={6}><Card><Statistic title="原值合计" value={totalOriginal} precision={2} prefix="¥" /></Card></Col>
                  <Col span={6}><Card><Statistic title="累计折旧" value={totalAccumulated} precision={2} prefix="¥" /></Card></Col>
                  <Col span={6}><Card><Statistic title="账面净值合计" value={totalNetValue} precision={2} prefix="¥" /></Card></Col>
                </Row>
                <Card>
                  <Table dataSource={data} columns={ledgerColumns} rowKey="assetId" loading={loading} pagination={{ pageSize: 10 }} scroll={{ x: 1200 }} />
                </Card>
              </>
            ),
          },
          {
            key: 'category',
            label: '按分类汇总',
            children: (
              <>
                <Row gutter={16} style={{ marginBottom: 24 }}>
                  <Col span={6}><Card><Statistic title="资产总数" value={catTotals.count} /></Card></Col>
                  <Col span={6}><Card><Statistic title="原值合计" value={catTotals.original} precision={2} prefix="¥" /></Card></Col>
                  <Col span={6}><Card><Statistic title="累计折旧" value={catTotals.accumulated} precision={2} prefix="¥" /></Card></Col>
                  <Col span={6}><Card><Statistic title="账面净值" value={catTotals.net} precision={2} prefix="¥" /></Card></Col>
                </Row>
                <Card>
                  <Table dataSource={categorySummary} columns={summaryColumns} rowKey="groupKey" loading={summaryLoading} pagination={false} />
                </Card>
              </>
            ),
          },
          {
            key: 'department',
            label: '按部门汇总',
            children: (
              <>
                <Row gutter={16} style={{ marginBottom: 24 }}>
                  <Col span={6}><Card><Statistic title="资产总数" value={deptTotals.count} /></Card></Col>
                  <Col span={6}><Card><Statistic title="原值合计" value={deptTotals.original} precision={2} prefix="¥" /></Card></Col>
                  <Col span={6}><Card><Statistic title="累计折旧" value={deptTotals.accumulated} precision={2} prefix="¥" /></Card></Col>
                  <Col span={6}><Card><Statistic title="账面净值" value={deptTotals.net} precision={2} prefix="¥" /></Card></Col>
                </Row>
                <Card>
                  <Table dataSource={deptSummary} columns={summaryColumns} rowKey="groupKey" loading={summaryLoading} pagination={false} />
                </Card>
              </>
            ),
          },
        ]}
      />
    </div>
  );
}
