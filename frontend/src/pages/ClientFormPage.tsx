import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Input, Textarea, Button } from '../components/UI';
import { getClient, createClient, updateClient, deleteClient } from '../services/api';
import { ClientFormData } from '../types';
import { TrashIcon } from '@heroicons/react/24/outline';

const ClientFormPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEditing = id !== 'new';

  const [formData, setFormData] = useState<ClientFormData>({
    name: '',
    address: '',
    contact_person: '',
    default_hourly_rate: 30, // Default rate
  });
  const [isLoading, setIsLoading] = useState(isEditing);
  const [isSaving, setIsSaving] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchClient = async () => {
      if (!isEditing) return;

      try {
        const data = await getClient(parseInt(id as string));
        setFormData({
          name: data.name,
          address: data.address,
          contact_person: data.contact_person || '',
          default_hourly_rate: data.default_hourly_rate,
        });
        setError('');
      } catch (error) {
        console.error('Error fetching client:', error);
        setError('Failed to load client data. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchClient();
  }, [id, isEditing]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target as HTMLInputElement;

    if (type === 'number') {
      setFormData((prev) => ({
        ...prev,
        [name]: parseFloat(value) || 0,
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setError('');

    try {
      if (isEditing) {
        await updateClient(parseInt(id as string), formData);
      } else {
        await createClient(formData);
      }
      navigate('/clients');
    } catch (error) {
      console.error('Error saving client:', error);
      setError('Failed to save client. Please try again.');
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this client?')) {
      return;
    }

    setIsDeleting(true);
    setError('');

    try {
      await deleteClient(parseInt(id as string));
      navigate('/clients');
    } catch (error) {
      console.error('Error deleting client:', error);
      setError('Failed to delete client. Please try again.');
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <p className="text-gray-600">Loading client data...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">
          {isEditing ? 'Edit Client' : 'New Client'}
        </h1>
        {isEditing && (
          <Button
            variant="danger"
            onClick={handleDelete}
            disabled={isDeleting}
            className="flex items-center"
          >
            <TrashIcon className="w-5 h-5 mr-1" />
            {isDeleting ? 'Deleting...' : 'Delete Client'}
          </Button>
        )}
      </div>

      <Card>
        {error && (
          <div className="mb-4 p-2 bg-red-100 text-red-800 rounded">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <Input
            id="name"
            name="name"
            label="Client Name"
            value={formData.name}
            onChange={handleChange}
            required
          />

          <Textarea
            id="address"
            name="address"
            label="Address"
            value={formData.address}
            onChange={handleChange}
            required
          />

          <Input
            id="contact_person"
            name="contact_person"
            label="Contact Person (optional)"
            value={formData.contact_person}
            onChange={handleChange}
          />

          <Input
            id="default_hourly_rate"
            name="default_hourly_rate"
            label="Default Hourly Rate (€)"
            type="number"
            value={formData.default_hourly_rate}
            onChange={handleChange}
            min={0}
            step={0.01}
            required
          />

          <div className="flex justify-between mt-6">
            <Button
              variant="secondary"
              onClick={() => navigate('/clients')}
              type="button"
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save Client'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default ClientFormPage;
