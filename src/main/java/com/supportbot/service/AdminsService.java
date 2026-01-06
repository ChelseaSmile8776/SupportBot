package com.supportbot.service;

import com.supportbot.domain.AdminGroup;
import com.supportbot.repo.GroupAdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminsService {
    private final GroupAdminRepository groupAdmins;

    public AdminsService(GroupAdminRepository groupAdmins) {
        this.groupAdmins = groupAdmins;
    }

    public String buildAdmins(AdminGroup g) {
        var list = groupAdmins.findByAdminGroupIdOrderByRatingAvgDesc(g.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("👮 <b>Админы</b>\n\n");

        if (list.isEmpty()) {
            sb.append("Пока никого нет.\n");
            return sb.toString();
        }

        for (var a : list) {
            sb.append("• <code>").append(a.getTelegramUserId()).append("</code>")
                    .append(" — ").append(a.getRole());

            if (a.getRatingCount() > 0 && a.getRatingAvg() != null) {
                sb.append(" — ⭐ ").append(a.getRatingAvg()).append(" (").append(a.getRatingCount()).append(")");
            } else {
                sb.append(" — ⭐ —");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}