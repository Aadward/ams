import { useState } from 'react';
import { Form, Input, InputNumber, Button, Card, message, Space, DatePicker } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { insuranceApi } from '../api/insurance';

const { TextArea } = Input;

export default function ClaimForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const numericId = Number(id);
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  const { data: insurance, isLoading } = useQuery({
    queryKey: ['insurance-policy', numericId],
    queryFn: async () => {
      const { data } = await insuranceApi.getById(numericId);
      return data;
    },
    enabled: Boolean(id),
  });

  const claimMutation = useMutation({
    mutationFn: (values: { claimNumber: string; incidentDate: string; claimAmount: number; incidentDescription: string }) =>
      insuranceApi.createClaim({ policyId: numericId, ...values }),
    onSuccess: () => {
      message.success('理赔申请提交成功');
      navigate(`/insurance/${id}`);
    },
    onError: () => {
      message.error('理赔申请提交失败');
    },
  });

  const handleSubmit = async (values: { claimNumber: string; incidentDate: dayjs.Dayjs; claimAmount: number; incidentDescription: string }) => {
    setSubmitting(true);
    try {
      await claimMutation.mutateAsync({
        ...values,
        incidentDate: values.incidentDate.format('YYYY-MM-DD'),
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (isLoading) {
    return <Card loading />;
  }

  if (!insurance) {
    return (
      <Card>
        <div>保险记录不存在</div>
        <Button onClick={() => navigate('/insurance')}>返回列表</Button>
      </Card>
    );
  }

  const canClaim = insurance.status === 'ACTIVE';

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h2>申请理赔</h2>
      </div>

      <Card
        title="保险信息"
        style={{ marginBottom: 16 }}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Space>
            <strong>资产：</strong>
            <span>{insurance.assetName} ({insurance.assetCode})</span>
          </Space>
          <Space>
            <strong>保单号：</strong>
            <span>{insurance.policyNumber}</span>
          </Space>
          <Space>
            <strong>保险公司：</strong>
            <span>{insurance.insuranceCompany}</span>
          </Space>
          <Space>
            <strong>保险金额：</strong>
            <span>¥{insurance.coverageAmount?.toLocaleString()}</span>
          </Space>
          <Space>
            <strong>到期日期：</strong>
            <span style={{ color: dayjs(insurance.endDate).isBefore(dayjs().add(30, 'day')) ? '#ff4d4f' : 'inherit' }}>
              {insurance.endDate}
              {dayjs(insurance.endDate).isBefore(dayjs()) && ' (已过期)'}
            </span>
          </Space>
        </Space>
      </Card>

      {!canClaim && (
        <Card>
          <div style={{ color: '#ff4d4f', textAlign: 'center', padding: 24 }}>
            该保险已过期或已取消，无法申请理赔。
          </div>
          <Space style={{ display: 'flex', justifyContent: 'center' }}>
            <Button onClick={() => navigate(`/insurance/${id}`)}>
              返回详情
            </Button>
            <Button onClick={() => navigate('/insurance')}>
              返回列表
            </Button>
          </Space>
        </Card>
      )}

      {canClaim && (
        <Card title="理赔申请">
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
          >
            <Form.Item
              name="claimNumber"
              label="索赔单号"
              rules={[
                { required: true, message: '请输入索赔单号' },
              ]}
            >
              <Input placeholder="请输入索赔单号" />
            </Form.Item>

            <Form.Item
              name="incidentDate"
              label="出险日期"
              rules={[
                { required: true, message: '请选择出险日期' },
              ]}
            >
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item
              name="claimAmount"
              label="索赔金额（元）"
              rules={[
                { required: true, message: '请输入索赔金额' },
                {
                  type: 'number',
                  min: 0.01,
                  message: '索赔金额必须大于0',
                },
                {
                  type: 'number',
                  max: insurance.coverageAmount,
                  message: `索赔金额不能超过保险金额 ¥${insurance.coverageAmount.toLocaleString()}`,
                },
              ]}
            >
              <InputNumber
                style={{ width: '100%' }}
                min={0.01}
                max={insurance.coverageAmount}
                precision={2}
                placeholder="请输入索赔金额"
                formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              />
            </Form.Item>

            <Form.Item
              name="incidentDescription"
              label="出险经过"
              rules={[
                { required: true, message: '请输入出险经过' },
                { min: 5, message: '出险经过至少5个字符' },
              ]}
            >
              <TextArea
                rows={4}
                placeholder="请详细描述出险经过（至少5个字符）"
              />
            </Form.Item>

            <Form.Item>
              <Space>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={submitting}
                >
                  提交理赔申请
                </Button>
                <Button onClick={() => navigate(`/insurance/${id}`)}>
                  取消
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Card>
      )}
    </div>
  );
}