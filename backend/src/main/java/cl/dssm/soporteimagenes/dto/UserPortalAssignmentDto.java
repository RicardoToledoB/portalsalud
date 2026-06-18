package cl.dssm.soporteimagenes.dto;

public record UserPortalAssignmentDto(
        Long portalId,
        String portalName,
        Long topicId,
        String topicName
) {}
