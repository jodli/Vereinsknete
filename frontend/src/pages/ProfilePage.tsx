import React, { useState, useEffect } from 'react';
import { Card, Input, Textarea, Button, LoadingState } from '../components/UI';
import { getUserProfile, updateUserProfile } from '../services/api';
import { UserProfileFormData } from '../types';
import { useLanguage } from '../i18n';
import { useToast } from '../components/Toast';
import { UserIcon, BuildingOfficeIcon, CreditCardIcon } from '@heroicons/react/24/outline';

const ProfilePage: React.FC = () => {
    const [formData, setFormData] = useState<UserProfileFormData>({
        name: '',
        address: '',
        tax_id: '',
        bank_details: '',
    });
    const { translations } = useLanguage();
    const { success, error: showError } = useToast();
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);

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

        try {
            await updateUserProfile({
                name: formData.name,
                address: formData.address,
                tax_id: formData.tax_id || undefined,
                bank_details: formData.bank_details || undefined,
            });

            success(translations.profile.success.saved, 'Your profile information has been updated successfully.');
        } catch (error) {
            console.error('Error updating profile:', error);
            showError('Failed to update profile', error instanceof Error ? error.message : 'An unexpected error occurred');
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading) {
        return <LoadingState message={translations.common.loading} />;
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-gray-900">{translations.profile.title}</h1>
                <p className="text-gray-600 mt-1">
                    Manage your personal and business information for invoices
                </p>
            </div>

            {/* Form */}
            <div className="max-w-2xl">
                <Card>
                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div className="mb-6">
                            <p className="text-sm text-gray-600">
                                Your information will be used on invoices. Make sure all details are correct.
                            </p>
                        </div>

                        {/* Personal Information */}
                        <fieldset className="border border-gray-200 rounded-lg p-6">
                            <legend className="text-lg font-medium text-gray-900 px-2">
                                {translations.profile.personalInfo}
                            </legend>
                            <div className="space-y-4 mt-4">

                                <Input
                                    id="name"
                                    name="name"
                                    label={translations.profile.labels.name}
                                    value={formData.name}
                                    onChange={handleChange}
                                    leftIcon={<UserIcon />}
                                    required
                                />

                                <Textarea
                                    id="address"
                                    name="address"
                                    label={translations.profile.labels.address}
                                    value={formData.address}
                                    onChange={handleChange}
                                    rows={3}
                                    helpText="Full business address including street, city, and postal code"
                                    required
                                />
                            </div>
                        </fieldset>

                        {/* Business Information */}
                        <fieldset className="border border-gray-200 rounded-lg p-6">
                            <legend className="text-lg font-medium text-gray-900 px-2">
                                {translations.profile.businessInfo}
                            </legend>
                            <div className="space-y-4 mt-4">
                                <Input
                                    id="tax_id"
                                    name="tax_id"
                                    label={translations.profile.labels.taxId}
                                    value={formData.tax_id}
                                    onChange={handleChange}
                                    leftIcon={<BuildingOfficeIcon />}
                                    helpText="Optional: Your tax identification number"
                                />
                            </div>
                        </fieldset>

                        {/* Bank Information */}
                        <fieldset className="border border-gray-200 rounded-lg p-6">
                            <legend className="text-lg font-medium text-gray-900 px-2">
                                {translations.profile.bankInfo}
                            </legend>
                            <div className="mt-4">
                                <Textarea
                                    id="bank_details"
                                    name="bank_details"
                                    label={translations.profile.labels.bankDetails}
                                    value={formData.bank_details}
                                    onChange={handleChange}
                                    placeholder={translations.profile.placeholders.bankDetails}
                                    rows={3}
                                    helpText={translations.profile.hints.placeholders}
                                />
                            </div>
                        </fieldset>

                        {/* Form Actions */}
                        <div className="flex flex-col sm:flex-row sm:justify-end gap-4 pt-6 border-t border-gray-200">
                            <Button 
                                type="submit" 
                                disabled={isSaving}
                                loading={isSaving}
                            >
                                {translations.profile.buttons.save}
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
};

export default ProfilePage;
