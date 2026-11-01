package hotel.util;

import hotel.config.AppConfig;
import hotel.model.Guest;
import hotel.model.Invoice;
import hotel.model.InvoiceItem;
import hotel.model.Reservation;

/**
 * Generates formatted plain-text and HTML itemized invoices for billing, export, and printing.
 */
public final class InvoiceGenerator {

    private InvoiceGenerator() {}

    public static String generateHtmlInvoice(Invoice invoice, Reservation res, Guest guest) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>");
        sb.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 30px; color: #1e293b; }");
        sb.append(".header { border-bottom: 3px solid #d4af37; padding-bottom: 15px; margin-bottom: 20px; }");
        sb.append(".hotel-title { font-size: 24px; font-weight: bold; color: #0f172a; }");
        sb.append(".hotel-sub { font-size: 13px; color: #64748b; }");
        sb.append(".meta-box { display: flex; justify-content: space-between; margin-bottom: 25px; }");
        sb.append(".meta-col { width: 48%; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 15px; margin-bottom: 20px; }");
        sb.append("th { background-color: #0f172a; color: #ffffff; text-align: left; padding: 10px; font-size: 13px; }");
        sb.append("td { padding: 9px 10px; border-bottom: 1px solid #e2e8f0; font-size: 13px; }");
        sb.append(".total-section { width: 350px; margin-left: auto; margin-top: 15px; }");
        sb.append(".total-row { display: flex; justify-content: space-between; padding: 5px 0; font-size: 13px; }");
        sb.append(".grand-total { font-size: 16px; font-weight: bold; color: #0f172a; border-top: 2px solid #0f172a; padding-top: 8px; }");
        sb.append(".badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; }");
        sb.append(".badge-paid { background-color: #dcfce7; color: #15803d; }");
        sb.append(".badge-due { background-color: #fee2e2; color: #b91c1c; }");
        sb.append(".footer { margin-top: 40px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; padding-top: 15px; }");
        sb.append("</style></head><body>");

        sb.append("<div class='header'>");
        sb.append("<div class='hotel-title'>").append(AppConfig.getHotelName()).append("</div>");
        sb.append("<div class='hotel-sub'>Official Guest Folio & Itemized Invoice</div>");
        sb.append("</div>");

        sb.append("<table style='margin-bottom:20px; border:none;'><tr>");
        sb.append("<td style='border:none; width:50%; vertical-align:top;'>");
        sb.append("<strong>Billed To:</strong><br>");
        if (guest != null) {
            sb.append(guest.getFullName()).append("<br>");
            sb.append("Email: ").append(guest.getEmail()).append("<br>");
            sb.append("Phone: ").append(guest.getPhone()).append("<br>");
        } else {
            sb.append("Guest Account #").append(invoice.getGuestId()).append("<br>");
        }
        sb.append("</td>");

        sb.append("<td style='border:none; width:50%; text-align:right; vertical-align:top;'>");
        sb.append("<strong>Invoice #:</strong> ").append(invoice.getInvoiceNumber()).append("<br>");
        if (res != null) {
            sb.append("<strong>Booking Ref:</strong> ").append(res.getReferenceNumber()).append("<br>");
            sb.append("<strong>Stay:</strong> ").append(DateUtil.formatDate(res.getCheckInDate()))
                    .append(" to ").append(DateUtil.formatDate(res.getCheckOutDate()))
                    .append(" (").append(res.getNights()).append(" nights)<br>");
        }
        sb.append("<strong>Status:</strong> ");
        if ("PAID".equalsIgnoreCase(invoice.getStatus().name())) {
            sb.append("<span class='badge badge-paid'>PAID IN FULL</span>");
        } else {
            sb.append("<span class='badge badge-due'>").append(invoice.getStatus().getDisplayName()).append("</span>");
        }
        sb.append("</td></tr></table>");

        sb.append("<table><thead><tr>");
        sb.append("<th>Description</th><th style='text-align:center;'>Qty</th><th style='text-align:right;'>Unit Rate</th><th style='text-align:right;'>Total</th>");
        sb.append("</tr></thead><tbody>");

        if (invoice.items() != null && !invoice.items().isEmpty()) {
            for (InvoiceItem item : invoice.items()) {
                sb.append("<tr>");
                sb.append("<td>").append(item.getDescription()).append("</td>");
                sb.append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>");
                sb.append("<td style='text-align:right;'>").append(CurrencyUtil.format(item.getUnitPrice())).append("</td>");
                sb.append("<td style='text-align:right;'>").append(CurrencyUtil.format(item.getTotalPrice())).append("</td>");
                sb.append("</tr>");
            }
        } else {
            sb.append("<tr><td colspan='4'>Room charges and accommodation package</td></tr>");
        }
        sb.append("</tbody></table>");

        sb.append("<table style='width:320px; margin-left:auto; border:none;'>");
        sb.append("<tr><td style='border:none;'>Subtotal:</td><td style='border:none; text-align:right;'>").append(CurrencyUtil.format(invoice.getSubtotal())).append("</td></tr>");
        sb.append("<tr><td style='border:none;'>Taxes & Fees:</td><td style='border:none; text-align:right;'>").append(CurrencyUtil.format(invoice.getTaxAmount())).append("</td></tr>");
        if (invoice.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append("<tr><td style='border:none;'>Discount:</td><td style='border:none; text-align:right;'>-").append(CurrencyUtil.format(invoice.getDiscountAmount())).append("</td></tr>");
        }
        sb.append("<tr style='border-top:2px solid #0f172a; font-weight:bold;'><td style='border:none;'>Total Charged:</td><td style='border:none; text-align:right;'>").append(CurrencyUtil.format(invoice.getTotalAmount())).append("</td></tr>");
        sb.append("<tr><td style='border:none;'>Amount Paid:</td><td style='border:none; text-align:right;'>").append(CurrencyUtil.format(invoice.getPaidAmount())).append("</td></tr>");
        sb.append("<tr style='font-size:15px; font-weight:bold; color:").append(invoice.getBalanceDue().compareTo(java.math.BigDecimal.ZERO) > 0 ? "#dc2626" : "#16a34a").append(";'><td style='border:none;'>Balance Due:</td><td style='border:none; text-align:right;'>").append(CurrencyUtil.format(invoice.getBalanceDue())).append("</td></tr>");
        sb.append("</table>");

        sb.append("<div class='footer'>");
        sb.append("Thank you for staying at ").append(AppConfig.getHotelName()).append("!<br>");
        sb.append("For inquiries, contact frontdesk@grandstay.com | Guest Assistance 24/7");
        sb.append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }
}
