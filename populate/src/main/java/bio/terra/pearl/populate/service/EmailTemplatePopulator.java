package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.populate.dao.EmailTemplatePopulateDao;
import bio.terra.pearl.populate.dto.notifications.EmailTemplatePopDto;
import bio.terra.pearl.populate.dto.notifications.LocalizedEmailTemplatePopDto;
import bio.terra.pearl.populate.dto.notifications.TriggerPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailTemplatePopulator extends BasePopulator<EmailTemplate, EmailTemplatePopDto, PortalPopulateContext> {
    private EmailTemplateService emailTemplateService;
    private PortalService portalService;
    private EmailTemplatePopulateDao emailTemplatePopulateDao;
    private PortalEnvironmentService portalEnvironmentService;

    public EmailTemplatePopulator(EmailTemplateService emailTemplateService,
                                  PortalService portalService,
                                  EmailTemplatePopulateDao emailTemplatePopulateDao,
                                  PortalEnvironmentService portalEnvironmentService) {
        this.emailTemplateService = emailTemplateService;
        this.portalService = portalService;
        this.emailTemplatePopulateDao = emailTemplatePopulateDao;
        this.portalEnvironmentService = portalEnvironmentService;
    }

    public Trigger convertTrigger(TriggerPopDto configPopDto, PortalPopulateContext context) {
        Trigger config = new Trigger();
        BeanUtils.copyProperties(configPopDto, config);
        PortalEnvironment portalEnv = portalEnvironmentService
                .findOne(context.getPortalShortcode(), context.getEnvironmentName()).get();
        config.setPortalEnvironmentId(portalEnv.getId());

        EmailTemplate template;
        if (configPopDto.getPopulateFileName() != null) {
            template = context.fetchFromPopDto(configPopDto, emailTemplateService).orElseThrow(
                    () -> new IllegalArgumentException("Email template not found %s".formatted(configPopDto.getPopulateFileName())));
        } else {
            template = emailTemplateService.findByStableIdAndPortalShortcode(configPopDto.getEmailTemplateStableId(),
                    configPopDto.getEmailTemplateVersion(), context.getPortalShortcode()).orElseThrow(
                    () -> new IllegalArgumentException("Email template not found %s".formatted(configPopDto.getEmailTemplateStableId())));
        }
        config.setEmailTemplateId(template.getId());
        config.setEmailTemplate(template);
        return config;
    }

    @Override
    protected void preProcessDto(EmailTemplatePopDto popDto, PortalPopulateContext context) throws IOException  {
        popDto.setStableId(context.applyShortcodeOverride(popDto.getStableId()));
        UUID portalId = portalService.findOneByShortcode(context.getPortalShortcode()).orElse(
                /** if the context doesn't have a portal, it's because we're populating admin config
                 * so we want the portalId to be null
                 * eventually we might want to create a "Config" portal for this purpose so that the templates
                 * are UI editable */
                new Portal()
        ).getId();
        popDto.setPortalId(portalId);

        for(LocalizedEmailTemplatePopDto localizedEmailTemplatePopDto : popDto.getLocalizedEmailTemplateDtos()) {
            String bodyContent = filePopulateService.readFile(localizedEmailTemplatePopDto.getBodyPopulateFile(), context);
            localizedEmailTemplatePopDto.setBody(bodyContent);
            localizedEmailTemplatePopDto.setEmailTemplateId(popDto.getId());
        }

        popDto.getLocalizedEmailTemplates().addAll(popDto.getLocalizedEmailTemplateDtos());
    }

    @Override
    protected Class<EmailTemplatePopDto> getDtoClazz() {
        return EmailTemplatePopDto.class;
    }

    @Override
    public Optional<EmailTemplate> findFromDto(EmailTemplatePopDto popDto, PortalPopulateContext context) {
        Optional<EmailTemplate> existingOpt = context.fetchFromPopDto(popDto, emailTemplateService);
        if (existingOpt.isPresent()) {
            return existingOpt;
        }
        return emailTemplateService.findByStableIdAndPortalShortcode(popDto.getStableId(), popDto.getVersion(), context.getPortalShortcode());
    }

    @Override
    public EmailTemplate overwriteExisting(EmailTemplate existingObj, EmailTemplatePopDto popDto, PortalPopulateContext context) {
        // don't delete the template, since it may have other entities attached to it. Just mod the content
        existingObj.setLocalizedEmailTemplates(popDto.getLocalizedEmailTemplates());
        existingObj.setPortalId(popDto.getPortalId());
        return emailTemplatePopulateDao.update(existingObj);
    }

    @Override
    public EmailTemplate createPreserveExisting(EmailTemplate existingObj, EmailTemplatePopDto popDto, PortalPopulateContext context) {
        if (Objects.equals(existingObj.getLocalizedEmailTemplates(), popDto.getLocalizedEmailTemplates())) {
            // the things are the same, don't bother creating a new version
            return existingObj;
        }
        int newVersion = emailTemplateService.getNextVersionByPortalShortcode(popDto.getStableId(), context.getPortalShortcode());
        popDto.setVersion(newVersion);
        return emailTemplateService.create(popDto);
    }

    @Override
    public EmailTemplate createNew(EmailTemplatePopDto popDto, PortalPopulateContext context, boolean overwrite) {
        return emailTemplateService.create(popDto);
    }
}
