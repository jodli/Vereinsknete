import React, { useState, useEffect } from 'react';
import { Card, Input, Textarea, Button } from '../components/UI';
import { getUserProfile, updateUserProfile } from '../services/api';
import { UserProfileFormData } from '../types';
import { useLanguage } from '../i18n';

const ProfilePage: React.FC = () => {
    const [formData, setFormData] = useState<UserProfileFormData>({
        name: '',
        address: '',
        tax_id: '',
        bank_details: '',
    });
    const { translations } = useLanguage();
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const data = await getUserProfile();
                if (data) {
                    setFormData({
                        name: data.name,
                        address: data.address,
                        tax_id: data.tax_id || '',
                        bank_details: data.bank_details || '',
                    });
                }
            } catch (error) {
                console.error('Error fetching profile:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchProfile();
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        setSuccessMessage('');

        try {
            await updateUserProfile({
                name: formData.name,
                address: formData.address,
                tax_id: formData.tax_id || undefined,
                bank_details: formData.bank_details || undefined,
            });

            setSuccessMessage(translations.profile.success.saved);

            // Clear success message after 3 seconds
            setTimeout(() => {
                setSuccessMessage('');
            }, 3000);
        } catch (error) {
            console.error('Error updating profile:', error);
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <p className="text-gray-600">{translations.common.loading}</p>
            </div>
        );
    }

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">{translations.profile.title}</h1>
            <Card>
                <form onSubmit={handleSubmit}>
                    <div className="mb-6">
                        <p className="text-sm text-gray-600 mb-4">
                            Ihre Daten werden auf Rechnungen verwendet. Stellen Sie sicher, dass alle Angaben korrekt sind.
                        </p>
                        {successMessage && (
                            <div className="mb-4 p-2 bg-green-100 text-green-800 rounded">
                                {successMessage}
                            </div>
                        )}
                    </div>

                    <Input
                        id="name"
                        name="name"
                        label={translations.profile.labels.name}
                        value={formData.name}
                        onChange={handleChange}
                        required
                    />

                    <Textarea
                        id="address"
                        name="address"
                        label={translations.profile.labels.address}
                        value={formData.address}
                        onChange={handleChange}
                        required
                    />

                    <Input
                        id="tax_id"
                        name="tax_id"
                        label={`${translations.profile.labels.taxId} (optional)`}
                        value={formData.tax_id}
                        onChange={handleChange}
                    />

                    <Textarea
                        id="bank_details"
                        name="bank_details"
                        label={translations.profile.labels.bankDetails}
                        value={formData.bank_details}
                        onChange={handleChange}
                        placeholder={translations.profile.placeholders.bankDetails}
                    />
                    <p className="text-sm text-blue-600 mt-1 mb-4">
                        💡 {translations.profile.hints.placeholders}
                    </p>

                    <div className="flex justify-end mt-6">
                        <Button type="submit" disabled={isSaving}>
                            {isSaving ? `${translations.common.loading}...` : translations.profile.buttons.save}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    );
};

export default ProfilePage;
