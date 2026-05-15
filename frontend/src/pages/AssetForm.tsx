import { Form, Input, Select, DatePicker, InputNumber, Button, Card, Space, message, Upload, Image } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAsset, useCreateAsset, useUpdateAsset, useUploadAssetPhoto } from '../api/asset';
import { UploadOutlined } from '@ant-design/icons';
import type { RcFile } from 'antd/es/upload';

const { Option } = Select;

export default function AssetForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const isEdit = !!id;
  const numericId = Number(id);
  const [photoFile, setPhotoFile] = useState<RcFile | null>(null);
  const [photoUrl, setPhotoUrl] = useState<string | undefined>(undefined);

  const { data: asset, isLoading } = useAsset(numericId);
  const createMutation = useCreateAsset();
  const updateMutation = useUpdateAsset(numericId);
  const uploadPhotoMutation = useUploadAssetPhoto();

  useEffect(() => {
    if (isEdit && asset) {
      form.setFieldsValue({
        assetCode: asset.assetCode,
        name: asset.name,
        category: asset.category,
        spec: asset.spec,
        location: asset.location,
        purchaseDate: asset.purchaseDate,
        purchasePrice: asset.purchasePrice,
        warrantyEnd: asset.warrantyEnd,
        supplier: asset.supplier,
      });
      setPhotoUrl(asset.photoUrl);
    }
  }, [isEdit, asset, form]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      let createdId: number;
      if (isEdit) {
        await updateMutation.mutateAsync(values);
        createdId = numericId;
        message.success('更新成功');
      } else {
        const created = await createMutation.mutateAsync(values);
        createdId = created.id;
        message.success('创建成功');
      }
      // Upload photo if selected
      if (photoFile) {
        await uploadPhotoMutation.mutateAsync({ id: createdId, file: photoFile as File });
        message.success('照片上传成功');
      }
      navigate('/assets');
    } catch {
      message.error('操作失败');
    }
  };

  return (
    <Card title={isEdit ? '编辑资产' : '新建资产'} loading={isEdit && isLoading}>
      <Form
        form={form}
        layout="vertical"
        style={{ maxWidth: 600 }}
        onFinish={handleSubmit}
      >
        <Form.Item label="资产编码" name="assetCode" rules={[{ required: true }]}>
          <Input placeholder="如 PC-2024-0001" />
        </Form.Item>
        <Form.Item label="名称" name="name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item label="分类" name="category" rules={[{ required: true }]}>
          <Select>
            <Option value="HARDWARE">硬件设备</Option>
            <Option value="NETWORK">网络设备</Option>
            <Option value="PERIPHERAL">配件耗材</Option>
            <Option value="SOFTWARE_LICENSE">软件许可证</Option>
          </Select>
        </Form.Item>
        <Form.Item label="规格" name="spec">
          <Input.TextArea />
        </Form.Item>
        <Form.Item label="购入日期" name="purchaseDate">
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item label="价格" name="purchasePrice">
          <InputNumber min={0} style={{ width: '100%' }} addonAfter="元" />
        </Form.Item>
        <Form.Item label="保修截止" name="warrantyEnd">
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item label="供应商" name="supplier">
          <Input />
        </Form.Item>
        <Form.Item label="位置" name="location">
          <Input />
        </Form.Item>
        <Form.Item label="照片">
          {photoUrl && (
            <div style={{ marginBottom: 8 }}>
              <Image src={photoUrl} alt="preview" style={{ maxWidth: 200, borderRadius: 4 }} />
            </div>
          )}
          <Upload
            accept="image/*"
            showUploadList={false}
            beforeUpload={(file: RcFile) => {
              setPhotoFile(file);
              // Preview local file
              const reader = new FileReader();
              reader.onload = (e) => setPhotoUrl(e.target?.result as string);
              reader.readAsDataURL(file);
              return false;
            }}
          >
            <Button icon={<UploadOutlined />}>{photoFile ? '更换照片' : '上传照片'}</Button>
          </Upload>
          {photoFile && (
            <div style={{ marginTop: 4, fontSize: 12, color: '#999' }}>
              已选择: {photoFile.name}
            </div>
          )}
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>
              {isEdit ? '保存' : '创建'}
            </Button>
            <Button onClick={() => navigate('/assets')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}