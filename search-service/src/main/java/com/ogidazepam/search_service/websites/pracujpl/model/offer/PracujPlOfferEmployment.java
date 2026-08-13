package com.ogidazepam.search_service.websites.pracujpl.model.offer;

import java.util.List;

public record PracujPlOfferEmployment(
        List<PracujPlOfferPositionLevel> positionLevels,
        List<PracujPlOfferWorkSchedule> workSchedules,
        List<PracujPlOfferWorkMode> workModes
) {
}
