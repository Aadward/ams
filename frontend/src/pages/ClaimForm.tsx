import { useState } from 'react';
import { Form, Input, InputNumber, Button, Card, message, Space, Alert } from 'antd';
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
    queryKey: ['insurance', numericId],
    queryFn: async () => {
      const { data } = await insuranceApi.getById(numericId);
      return data;
    },
    enabled: Boolean(id),
  });

  const claimMutation = useMutation({
    mutationFn: (values: { claimAmount: number; claimReason: string }) =>
      insuranceApi.submitClaim({ insuranceId: numericId, ...values }),
    onSuccess: () => {
      message.success('理赔申请提交成功');
      navigate(`/insurance/${id}`);
    },
    onError: () => {
      message.error('理赔申请提交失败');
    },
  });

  const handleSubmit = async (values: { claimAmount: number; claimReason: string }) => {
    setSubmitting(true);
    try {
      await claimMutation.mutateAsync(values);
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

  const canClaim = insurance.status === 'ACTIVE' && insurance.claimStatus !== 'PENDING';
  const isPending = insurance.claimStatus === 'PENDING';

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h2>{isPending ? '理赔进度' : '申请理赔'}</h2>
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

      {isPending && (
        <Alert
          type="warning"
          message="理赔正在处理中"
          description={`您已于 ${insurance.claimDate} 提交了理赔申请，理赔金额 ¥${insurance.claimAmount?.toLocaleString()}，当前状态：待处理。`}
          style={{ marginBottom: 16 }}
          showIcon
        />
      )}

      {!canClaim && !isPending && (
        <Alert
          type="error"
          message="无法申请理赔"
          description={
            insurance.status !== 'ACTIVE'
              ? '该保险已过期或已取消，无法申请理赔。'
              : '该保险已有待处理或已完成的理赔申请。'
          }
          style={{ marginBottom: 16 }}
          showIcon
        />
      )}

      {canClaim && (
        <Card title="理赔申请">
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
            initialValues={{
              claimAmount: Math.min(insurance.coverageAmount, 0),
            }}
          >
            <Form.Item
              name="claimAmount"
              label="理赔金额（元）"
              rules={[
                { required: true, message: '请输入理赔金额' },
                {
                  type: 'number',
                  min: 0.01,
                  message: '理赔金额必须大于0',
                },
                {
                  type: 'number',
                  max: insurance.coverageAmount,
                  message: `理赔金额不能超过保险金额 ¥${insurance.coverageAmount.toLocaleString()}`,
                },
              ]}
            >
              <InputNumber
                style={{ width: '100%' }}
                min={0.01}
                max={insurance.coverageAmount}
                precision={2}
                placeholder="请输入理赔金额"
                formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              />
            </Form.Item>

            <Form.Item
              name="claimReason"
              label="理赔原因"
              rules={[
                { required: true, message: '请输入理赔原因' },
                { min: 5, message: '理赔原因至少5个字符' },
              ]}
            >
              <TextArea
                rows={4}
                placeholder="请详细描述理赔原因（至少5个字符）"
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

      {!canClaim && !isPending && (
        <Space>
          <Button onClick={() => navigate(`/insurance/${id}`)}>
            返回详情
          </Button>
          <Button onClick={() => navigate('/insurance')}>
            返回列表
          </Button>
        </Space>
      )}
    </div>
  );
}
