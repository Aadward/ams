import { useState, useEffect } from 'react';
import { Form, Input, InputNumber, DatePicker, Select, Button, Card, message, Space, Row, Col } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import dayjs from 'dayjs';
import { insuranceApi } from '../api/insurance';

const { TextArea } = Input;

const insuranceTypeOptions = [
  { label: '财产险', value: 'PROPERTY' },
  { label: '综合险', value: 'COMPREHENSIVE' },
  { label: '盗抢险', value: 'THEFT' },
];

export default function InsuranceForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(isEdit);

  useEffect(() => {
    if (isEdit && id) {
      insuranceApi
        .getById(Number(id))
        .then(res => {
          const data = res.data;
          form.setFieldsValue({
            ...data,
            startDate: data.startDate ? dayjs(data.startDate) : undefined,
            endDate: data.endDate ? dayjs(data.endDate) : undefined,
          });
        })
        .catch(() => {
          message.error('加载保险记录失败');
          navigate('/insurance');
        })
        .finally(() => setInitialLoading(false));
    }
  }, [id, isEdit, form, navigate]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    setLoading(true);
    try {
      const payload = {
        ...values,
        startDate: values.startDate ? (values.startDate as dayjs.Dayjs).format('YYYY-MM-DD') : undefined,
        endDate: values.endDate ? (values.endDate as dayjs.Dayjs).format('YYYY-MM-DD') : undefined,
      };

      if (isEdit && id) {
        await insuranceApi.update(Number(id), payload as Parameters<typeof insuranceApi.update>[1]);
        message.success('更新成功');
      } else {
        await insuranceApi.create(payload as Parameters<typeof insuranceApi.create>[0]);
        message.success('创建成功');
      }
      navigate('/insurance');
    } catch {
      message.error(isEdit ? '更新失败' : '创建失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h2>{isEdit ? '编辑保险' : '新增保险'}</h2>
      </div>

      <Card loading={initialLoading}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            coverageAmount: 0,
            premium: 0,
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="assetId"
                label="资产ID"
                rules={[{ required: true, message: '请输入资产ID' }]}
              >
                <InputNumber style={{ width: '100%' }} placeholder="请输入资产ID" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="policyNumber"
                label="保单号"
                rules={[{ required: true, message: '请输入保单号' }]}
              >
                <Input placeholder="请输入保单号" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="insuranceCompany"
                label="保险公司"
              >
                <Input placeholder="请输入保险公司（可选）" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="type"
                label="保险类型"
                rules={[{ required: true, message: '请选择保险类型' }]}
              >
                <Select placeholder="请选择" options={insuranceTypeOptions} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="coverageAmount"
                label="保险金额（元）"
                rules={[{ required: true, message: '请输入保险金额' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  precision={2}
                  placeholder="请输入保险金额"
                  formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="premium"
                label="保费（元）"
              >
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  precision={2}
                  placeholder="请输入保费（可选）"
                  formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="startDate"
                label="生效日期"
                rules={[{ required: true, message: '请选择生效日期' }]}
              >
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="endDate"
                label="到期日期"
                rules={[{ required: true, message: '请选择到期日期' }]}
              >
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="remarks" label="备注">
            <TextArea rows={3} placeholder="请输入备注（可选）" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>
                {isEdit ? '更新' : '创建'}
              </Button>
              <Button onClick={() => navigate('/insurance')}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}