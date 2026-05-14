import { useState } from 'react';
import { Tree, Button, Space, Modal, Form, Input, message } from 'antd';
import { useDepartmentList, useCreateDepartment, useUpdateDepartment, useDeleteDepartment } from '../api/department';
import type { Department } from '../types';

export default function DepartmentList() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDept, setEditingDept] = useState<Department | null>(null);
  const [form] = Form.useForm();
  const { data: deptList, isLoading } = useDepartmentList();
  const createDept = useCreateDepartment();
  const updateDept = useUpdateDepartment(editingDept?.id || 0);
  const deleteDept = useDeleteDepartment();

  const handleAdd = () => {
    setEditingDept(null);
    form.resetFields();
    setIsModalOpen(true);
  };

  const handleEdit = (dept: Department) => {
    setEditingDept(dept);
    form.setFieldsValue({ name: dept.name, parentId: dept.parentId, description: dept.description });
    setIsModalOpen(true);
  };

  const handleDelete = (dept: Department) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除部门「${dept.name}」吗？`,
      onOk: () => deleteDept.mutateAsync(dept.id).then(() => message.success('删除成功')),
    });
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    try {
      if (editingDept) {
        await updateDept.mutateAsync(values);
        message.success('更新成功');
      } else {
        await createDept.mutateAsync(values);
        message.success('创建成功');
      }
      setIsModalOpen(false);
    } catch {
      message.error('操作失败');
    }
  };

  // Convert flat list to tree data for Ant Design Tree
  const convertToTreeData = (depts: Department[]): { key: number; title: string; children: { key: number; title: string }[] }[] => {
    return depts.map(dept => ({
      key: dept.id,
      title: (
        <Space>
          <span>{dept.name}</span>
          <Button size="small" onClick={(e) => { e.stopPropagation(); handleEdit(dept); }}>编辑</Button>
          <Button size="small" danger onClick={(e) => { e.stopPropagation(); handleDelete(dept); }}>删除</Button>
        </Space>
      ),
      children: dept.children && dept.children.length > 0 ? convertToTreeData(dept.children) : [],
    }));
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Space>
        <Button type="primary" onClick={handleAdd}>新建部门</Button>
      </Space>

      {deptList && deptList.length > 0 ? (
        <Tree
          treeData={convertToTreeData(deptList)}
          loading={isLoading}
          defaultExpandAll
        />
      ) : (
        <div style={{ color: '#999', padding: 16 }}>暂无部门，点击上方按钮创建</div>
      )}

      <Modal
        title={editingDept ? '编辑部门' : '新建部门'}
        open={isModalOpen}
        onOk={handleSubmit}
        onCancel={() => setIsModalOpen(false)}
        confirmLoading={createDept.isPending || updateDept.isPending}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="部门名称" rules={[{ required: true, message: '请输入部门名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="parentId" label="上级部门">
            <Input type="number" placeholder="留空表示根部门" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
