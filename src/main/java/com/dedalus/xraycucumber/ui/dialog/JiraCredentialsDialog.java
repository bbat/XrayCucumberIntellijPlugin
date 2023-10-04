package com.dedalus.xraycucumber.ui.dialog;

import javax.annotation.Nullable;
import javax.swing.*;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.ui.utils.ServiceParametersUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class JiraCredentialsDialog extends DialogWrapper {

    private final ServiceParameters serviceParameters;
    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JCheckBox storeCredentialsCheckBox;
    private JPanel rootPanel;

    public JiraCredentialsDialog(@Nullable Project project, ServiceParameters serviceParameters) {
        super(project);
        this.serviceParameters = serviceParameters;
        init();
        setTitle("Credential for " + serviceParameters.getUrl().toExternalForm());
        usernameTextField.setText(serviceParameters.getUsername());
        passwordPasswordField.setText(serviceParameters.getPassword());
        storeCredentialsCheckBox.setSelected(ServiceParametersUtils.storeByDefault());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    public ServiceParameters getUpdatedServiceParameters() {
        return new ServiceParameters.Builder()
                .url(serviceParameters.getUrl())
                .username(usernameTextField.getText())
                .password(String.copyValueOf(passwordPasswordField.getPassword()))
                .projectKey(serviceParameters.getProjectKey())
                .filterId(serviceParameters.getFilterId())
                .build();
    }

    public boolean storeCredentials() {
        return storeCredentialsCheckBox.isSelected();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return usernameTextField.getText().isEmpty() ? usernameTextField : passwordPasswordField;
    }
}
