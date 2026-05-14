import { useEffect, useState } from 'react';
import { DatePicker, Table, Card } from 'antd';
import dayjs, { Dayjs } from 'dayjs';
import { consumableApi } from '../api/consumable';
import { consumableCategoryLabels } from '../types';

const { RangePicker } = DatePicker;

export default function ConsumableReport() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [dates, setDates] = useState<[Dayjs, Dayjs] | null>([
    dayjs().startOf('month'),
    dayjs().endOf('month'),
  ]);

  const load = async () => {
    if (!dates) return;
    setLoading(true);
    try {
      const [start, end] = dates;
      const res = await consumableApi.getConsumptionReport(
        start.startOf('day').toISOString(),
        end.endOf('day').toISOString()
      );
      setData(res);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [dates]);

  const handleDateChange = (value: any) => {
    if (!value) { setDates(null); return; }
    setDates(value as [Dayjs, Dayjs]);
  };

  const columns = [
    { title: '易耗品', dataIndex: 'consumableName' },
    { 
      title: '分类', 
      dataIndex: 'category',
      render: (cat: string) => consumableCategoryLabels[cat] || cat,
    },
    { title: '消耗数量', dataIndex: 'totalQuantity' },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <RangePicker value={dates} onChange={handleDateChange} />
      </div>
      <Card title="消耗汇总" loading={loading}>
        <Table rowKey="consumableId" columns={columns} dataSource={data?.items || []} pagination={false} />
        <div style={{ marginTop: 16 }}>
          <p>总消耗数量：{data?.totalQuantity || 0}</p>
        </div>
      </Card>
    </div>
  );
}