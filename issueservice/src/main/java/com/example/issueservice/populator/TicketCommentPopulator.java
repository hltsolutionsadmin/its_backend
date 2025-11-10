package com.example.issueservice.populator;

import com.example.issueservice.model.TicketCommentModel;
import com.its.common.dto.TicketCommentDTO;
import com.its.common.populator.Populator;
import org.springframework.stereotype.Component;

@Component
public class TicketCommentPopulator implements Populator<TicketCommentModel, TicketCommentDTO> {

    @Override
    public void populate(TicketCommentModel source, TicketCommentDTO target) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setComment(source.getComment());
        target.setCreatedAt(source.getCreatedAt());

        if (source.getCreatedBy() != null) {
            target.setCreatedById(source.getCreatedBy());
//            target.setCreatedByName(source.getCreatedBy().getFullName());
        }

        if (source.getTicket() != null) {
            target.setTicketId(source.getTicket().getId());
        }
    }
}
