# -*- coding: utf-8 -*-
"""Generates a draw.io (.drawio) ER diagram of all 8 microservice databases."""
import html

FIELD_H = 16
HEADER_H = 26
BODY_PAD = 8
COL_GAP = 24
ROW_GAP = 24
CONTAINER_PAD_TOP = 34
CONTAINER_PAD = 16
META_COL_GAP = 60
META_ROW_GAP = 60

# service: (fill, stroke, header_fill)
SERVICES = [
    ("Auth Service", "#DCEAFB", "#5B84C4", "#5B84C4", {
        "Users": ["PK user_id", "FK↗Profile lichnost_id", "role", "created_at"],
        "Tokens": ["PK token_id", "FK user_id", "refresh_token_hash", "expires_at", "created_at"],
    }),
    ("Profile Service (Personal)", "#EDE4F7", "#8B5FBF", "#8B5FBF", {
        "User_profiles": ["PK lichnost_id", "FK group_id", "first_name", "last_name", "second_name",
                           "email", "card_number", "image", "membership_status"],
        "Groups": ["PK group_id", "title", "FK program_id", "FK proforg_id (почётно)"],
        "Programs": ["PK program_id", "title", "FK school_id", "FK proforg_id (почётно)"],
        "Schools": ["PK school_id", "title", "FK proforg_id (функциональный)"],
    }),
    ("Events Service", "#FCEBD8", "#D98A2B", "#D98A2B", {
        "Event": ["PK event_id", "title", "description", "short_description", "image",
                  "registration_start_at", "registration_end_at", "start_at", "end_at",
                  "available_group_ids", "FK↗Profile owner_id (шк. профорг)",
                  "FK↗Profile school_id (nullable)", "status (DRAFT/PUBLISHED/CANCELLED/ARCHIVED)",
                  "moderation_status (SUBMITTED/APPROVED/DEFERRED/REJECTED)",
                  "requested_points_per_attendee", "points_per_attendee",
                  "FK↗Auth reviewed_by (nullable)", "reviewed_at",
                  "is_registration_required", "created_at", "updated_at"],
        "VolunteerRecord": ["PK volunteer_record_id", "FK↗Profile lichnost_id", "FK event_id"],
    }),
    ("Mark Service (check_in_service)", "#DFF5F0", "#2FA98C", "#2FA98C", {
        "Registration": ["PK registration_id", "FK↗Profile lichnost_id", "FK↗Events event_id", "created_at"],
        "CheckIn": ["PK check_in_id", "FK registration_id", "FK↗Profile lichnost_id",
                    "type (SELF_SCAN/STAFF_SCAN)", "created_at"],
    }),
    ("Transactions Service", "#E4F5DE", "#4E9B3B", "#4E9B3B", {
        "Wallet": ["PK wallet_id", "wallet_type (PERSONAL/SCHOOL)",
                   "FK↗Profile owner_user_id (nullable)", "FK↗Profile school_id (nullable)",
                   "balance", "updated_at"],
        "Transaction": ["PK transaction_id", "FK sender_wallet_id (nullable)", "FK receiver_wallet_id (nullable)",
                         "type (EVENT_REWARD/TRANSFER/PURCHASE/REFUND)", "amount",
                         "status (PENDING/SUCCESS/FAILED)", "FK↗Events related_event_id (nullable)",
                         "FK↗Shop related_purchase_id (nullable)", "description", "created_at"],
    }),
    ("Shop Service", "#FBE4EC", "#C24E82", "#C24E82", {
        "Category": ["PK category_id", "FK parent_id (nullable)", "title", "created_at"],
        "Product": ["PK product_id", "FK category_id", "title", "description", "price (базовая)",
                    "status (DRAFT/PUBLISHED/ARCHIVED)", "FK cover_media_id (nullable)",
                    "created_at", "updated_at", "deleted_at"],
        "ProductVariant": ["PK variant_id", "FK product_id", "size (nullable, ENUM XS..XXL)",
                            "color (nullable)", "stock", "price_override (nullable)", "created_at"],
        "Product_media": ["PK media_id", "FK product_id", "media (url, только фото)",
                           "is_cover", "sort_order", "created_at"],
        "Purchase": ["PK purchase_id", "FK↗Profile buyer_id", "FK variant_id", "product_title",
                     "variant_label (nullable)", "unit_price", "count", "amount",
                     "status (PENDING/CONFIRMED/CANCELLED/REFUNDED)",
                     "FK↗Transactions transaction_id", "description", "created_at"],
    }),
    ("Storage Service", "#EAEAEA", "#7A7A7A", "#7A7A7A", {
        "StoredFile": ["PK file_id", "owner_service", "owner_id", "url", "content_type", "uploaded_at"],
    }),
    ("Notification Service", "#FEF6D8", "#C79A1E", "#C79A1E", {
        "Notification": ["PK notification_id", "FK↗Profile lichnost_id", "type",
                          "channel (PUSH/EMAIL)", "status (PENDING/SENT/FAILED)", "created_at"],
    }),
]

# (source_service, source_entity, target_service, target_entity, label, cross)
RELATIONS = [
    ("Auth Service", "Tokens", "Auth Service", "Users", "user_id", False),
    ("Auth Service", "Users", "Profile Service (Personal)", "User_profiles", "lichnost_id", True),
    ("Profile Service (Personal)", "User_profiles", "Profile Service (Personal)", "Groups", "group_id", False),
    ("Profile Service (Personal)", "Groups", "Profile Service (Personal)", "Programs", "program_id", False),
    ("Profile Service (Personal)", "Programs", "Profile Service (Personal)", "Schools", "school_id", False),
    ("Profile Service (Personal)", "Groups", "Profile Service (Personal)", "User_profiles", "proforg_id", False),
    ("Profile Service (Personal)", "Programs", "Profile Service (Personal)", "User_profiles", "proforg_id", False),
    ("Profile Service (Personal)", "Schools", "Profile Service (Personal)", "User_profiles", "proforg_id", False),
    ("Events Service", "Event", "Profile Service (Personal)", "User_profiles", "owner_id", True),
    ("Events Service", "Event", "Profile Service (Personal)", "Schools", "school_id", True),
    ("Events Service", "Event", "Auth Service", "Users", "reviewed_by", True),
    ("Events Service", "VolunteerRecord", "Events Service", "Event", "event_id", False),
    ("Events Service", "VolunteerRecord", "Profile Service (Personal)", "User_profiles", "lichnost_id", True),
    ("Mark Service (check_in_service)", "Registration", "Profile Service (Personal)", "User_profiles", "lichnost_id", True),
    ("Mark Service (check_in_service)", "Registration", "Events Service", "Event", "event_id", True),
    ("Mark Service (check_in_service)", "CheckIn", "Mark Service (check_in_service)", "Registration", "registration_id", False),
    ("Transactions Service", "Transaction", "Transactions Service", "Wallet", "sender_wallet_id", False),
    ("Transactions Service", "Transaction", "Transactions Service", "Wallet", "receiver_wallet_id", False),
    ("Transactions Service", "Wallet", "Profile Service (Personal)", "User_profiles", "owner_user_id", True),
    ("Transactions Service", "Wallet", "Profile Service (Personal)", "Schools", "school_id", True),
    ("Transactions Service", "Transaction", "Events Service", "Event", "related_event_id", True),
    ("Transactions Service", "Transaction", "Shop Service", "Purchase", "related_purchase_id", True),
    ("Shop Service", "Product", "Shop Service", "Category", "category_id", False),
    ("Shop Service", "Category", "Shop Service", "Category", "parent_id", False),
    ("Shop Service", "Product_media", "Shop Service", "Product", "product_id", False),
    ("Shop Service", "Product", "Shop Service", "Product_media", "cover_media_id", False),
    ("Shop Service", "ProductVariant", "Shop Service", "Product", "product_id", False),
    ("Shop Service", "Purchase", "Shop Service", "ProductVariant", "variant_id", False),
    ("Shop Service", "Purchase", "Profile Service (Personal)", "User_profiles", "buyer_id", True),
    ("Shop Service", "Purchase", "Transactions Service", "Transaction", "transaction_id", True),
    ("Notification Service", "Notification", "Profile Service (Personal)", "User_profiles", "lichnost_id", True),
]


def esc(s):
    return html.escape(s, quote=True)


def entity_size(fields, title):
    max_len = max([len(title)] + [len(f) for f in fields])
    w = max(200, min(340, int(max_len * 6.5) + 30))
    h = HEADER_H + BODY_PAD * 2 + len(fields) * FIELD_H
    return w, h


class Diagram:
    def __init__(self):
        self.cells = []
        self.id_counter = 0
        self.entity_group_id = {}  # (service, entity) -> cell id

    def next_id(self, prefix="c"):
        self.id_counter += 1
        return f"{prefix}{self.id_counter}"

    def add_cell(self, xml):
        self.cells.append(xml)

    def add_container(self, x, y, w, h, title, fill, stroke):
        cid = self.next_id("svc")
        self.add_cell(
            f'<mxCell id="{cid}" value="{esc(title)}" style="rounded=1;whiteSpace=wrap;html=1;'
            f'verticalAlign=top;align=left;spacingLeft=10;spacingTop=8;fontSize=15;fontStyle=1;'
            f'fillColor={fill};strokeColor={stroke};strokeWidth=2;opacity=55;fontColor=#222222;" '
            f'vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>'
        )
        return cid

    def add_entity(self, service, name, fields, x, y, w, h, header_fill):
        gid = self.next_id("ent")
        self.add_cell(
            f'<mxCell id="{gid}" value="" style="group" vertex="1" connectable="0" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>'
        )
        hid = self.next_id("hd")
        self.add_cell(
            f'<mxCell id="{hid}" value="{esc(name)}" style="rounded=0;whiteSpace=wrap;html=1;'
            f'align=center;verticalAlign=middle;fontSize=12;fontStyle=1;fontColor=#ffffff;'
            f'fillColor={header_fill};strokeColor=#333333;" vertex="1" parent="{gid}">'
            f'<mxGeometry x="0" y="0" width="{w}" height="{HEADER_H}" as="geometry"/></mxCell>'
        )
        body_h = h - HEADER_H
        lines = []
        for f in fields:
            is_key = f.startswith("PK") or f.startswith("FK")
            txt = html.escape(f, quote=False)
            lines.append(f"<b>{txt}</b>" if is_key else txt)
        raw_html = "<br>".join(lines)
        label = esc(raw_html)
        bid = self.next_id("bd")
        self.add_cell(
            f'<mxCell id="{bid}" value="{label}" style="rounded=0;whiteSpace=wrap;html=1;align=left;'
            f'verticalAlign=top;fontSize=10.5;spacingLeft=8;spacingTop=6;spacingBottom=6;'
            f'fillColor=#ffffff;strokeColor=#333333;" vertex="1" parent="{gid}">'
            f'<mxGeometry x="0" y="{HEADER_H}" width="{w}" height="{body_h}" as="geometry"/></mxCell>'
        )
        self.entity_group_id[(service, name)] = gid
        return gid

    def add_edge(self, src_id, dst_id, label, dashed):
        eid = self.next_id("e")
        style = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;fontSize=9;endArrow=none;"
                 "startArrow=diamondThin;startFill=1;strokeColor=#555555;")
        if dashed:
            style += "dashed=1;strokeColor=#999999;fontColor=#999999;"
        self.add_cell(
            f'<mxCell id="{eid}" value="{esc(label)}" style="{style}" edge="1" parent="1" '
            f'source="{src_id}" target="{dst_id}"><mxGeometry relative="1" as="geometry"/></mxCell>'
        )

    def render(self, total_w, total_h):
        body = "\n".join(self.cells)
        return f'''<mxfile host="app.diagrams.net" version="24.0.0">
  <diagram name="ПрофКом — БД всех микросервисов" id="db1">
    <mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{total_w}" pageHeight="{total_h}" math="0" shadow="0">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
{body}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''


def pack_entities(entities_dict, container_x, container_y):
    """Greedy 2-column packing of entities inside a service container."""
    items = []
    for name, fields in entities_dict.items():
        w, h = entity_size(fields, name)
        items.append((name, fields, w, h))
    items.sort(key=lambda it: -it[3])

    ncols = 2 if len(items) > 2 else 1
    col_heights = [CONTAINER_PAD_TOP] * ncols
    col_x_offsets = [0] * ncols
    col_widths = [0] * ncols

    # first pass: determine column width = max entity width in that column (assign greedily)
    placements = []
    for name, fields, w, h in items:
        col = col_heights.index(min(col_heights))
        placements.append((col, name, fields, w, h))
        col_heights[col] += h + ROW_GAP
        col_widths[col] = max(col_widths[col], w)

    # compute x offset per column
    x = CONTAINER_PAD
    for c in range(ncols):
        col_x_offsets[c] = x
        x += col_widths[c] + COL_GAP

    container_w = x - COL_GAP + CONTAINER_PAD
    container_h = max(col_heights) - ROW_GAP + CONTAINER_PAD

    col_y = [CONTAINER_PAD_TOP] * ncols
    result = []
    for col, name, fields, w, h in placements:
        ex = container_x + col_x_offsets[col]
        ey = container_y + col_y[col]
        result.append((name, fields, ex, ey, w, h))
        col_y[col] += h + ROW_GAP

    return result, container_w, container_h


def main():
    d = Diagram()
    meta_cols = 3
    meta_col_heights = [40] * meta_cols
    meta_col_x = [0] * meta_cols
    meta_col_widths = [0] * meta_cols

    # pre-compute container sizes to plan meta columns
    prelim = []
    for title, fill, stroke, header_fill, entities in SERVICES:
        placements, cw, ch = pack_entities(entities, 0, 0)
        prelim.append((title, fill, stroke, header_fill, entities, cw, ch))

    prelim.sort(key=lambda p: -p[6])

    x_cursor = 40
    col_assign = []
    for title, fill, stroke, header_fill, entities, cw, ch in prelim:
        col = meta_col_heights.index(min(meta_col_heights))
        col_assign.append((col, title, fill, stroke, header_fill, entities, cw, ch))
        meta_col_heights[col] += ch + META_ROW_GAP
        meta_col_widths[col] = max(meta_col_widths[col], cw)

    col_x = [0] * meta_cols
    x = 40
    for c in range(meta_cols):
        col_x[c] = x
        x += meta_col_widths[c] + META_COL_GAP
    total_w = x
    total_h = max(meta_col_heights) + 40

    col_y = [40] * meta_cols
    for col, title, fill, stroke, header_fill, entities, cw, ch in col_assign:
        cx = col_x[col]
        cy = col_y[col]
        d.add_container(cx, cy, cw, ch, title, fill, stroke)
        placements, _, _ = pack_entities(entities, cx, cy)
        for name, fields, ex, ey, w, h in placements:
            d.add_entity(title, name, fields, ex, ey, w, h, header_fill)
        col_y[col] += ch + META_ROW_GAP

    for s_svc, s_ent, t_svc, t_ent, label, cross in RELATIONS:
        src = d.entity_group_id.get((s_svc, s_ent))
        dst = d.entity_group_id.get((t_svc, t_ent))
        if src and dst:
            d.add_edge(src, dst, label, cross)

    xml = d.render(total_w, total_h)
    out_path = r"C:\Users\Антон\IdeaProjects\profkom\docs\ПрофКом_БД_диаграмма.drawio"
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(xml)
    print("saved:", out_path)


if __name__ == "__main__":
    main()
