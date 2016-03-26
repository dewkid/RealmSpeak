/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2016 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.io;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A class to send an e-mail message.  Requires Java Mail (mail.jar)
 * and Java Activation Framwork (activation.jar)
 */
public class SendMail {

    public static final String DEFAULT_SMTP_HOST = "mail";

    public static final String SMTP_KEY = "smtp";
    public static final String FROM_KEY = "from";
    public static final String TO_KEY = "to";
    public static final String CC_KEY = "cc";
    public static final String BCC_KEY = "bcc";

    private boolean htmlEnabled = false;

    private String from;
    private String[] recipientsTO;
    private String[] recipientsCC;
    private String[] recipientsBCC;
    private String subject;
    private String message;

    private ArrayList bodyParts;

    private String SMTP_Host;

    private String error = "";

    public SendMail(String cfgFileName) {
        readFile(new File(cfgFileName));
    }

    public SendMail(String inHost, String inFrom, String inTo) {
        Properties config = new Properties();
        config.put(SMTP_KEY, inHost);
        config.put(FROM_KEY, inFrom);
        config.put(TO_KEY, inTo);
        parseProperties(config);
    }

    public SendMail(String host, String from, Collection to, Collection cc,
                    Collection bcc) {
        Properties config = new Properties();
        config.put(SMTP_KEY, host);
        config.put(FROM_KEY, from);
        config.put(TO_KEY, makeCommaList(to));
        config.put(CC_KEY, makeCommaList(cc));
        config.put(BCC_KEY, makeCommaList(bcc));
        parseProperties(config);
    }

    private String makeCommaList(Collection list) {
        StringBuffer sb = new StringBuffer();
        if (list != null) {
            for (Iterator i = list.iterator(); i.hasNext(); ) {
                String val = i.next().toString();
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(val);
            }
        }
        return sb.toString();
    }

    public void addError(String err) {
        error = error + err;
    }

    public void setSubject(String val) {
        subject = val;
    }

    public void setMessage(String val) {
        message = val;
    }

    public boolean hasErrors() {
        return (error.length() > 0);
    }

    public String getError() {
        return error;
    }

    /**
     * Static method for convenience
     * Must provide at least smtpServerAddress, fromAddress, & toAddress
     */
    public static void sendMessage(String smtpServerAddress,
                                   String fromAddress,
                                   String toAddress,
                                   String subject,
                                   String message)
            throws MessagingException {

        if (smtpServerAddress == null || toAddress == null || fromAddress == null) {
            return;
        }
        if (subject == null) {
            subject = " ";
        }
        if (message == null) {
            message = " ";
        }
        SendMail mail = new SendMail(smtpServerAddress, fromAddress, toAddress);
        mail.setSubject(subject);
        mail.setMessage(message);
        mail.postMail();
    }

    /**
     * Static method for convenience
     * Sends mail with a single attached "file" created with the attachmentText
     * as the message and attachmentFileName as the filename
     * Must provide at least smtpServerAddress, fromAddress, toAddress,
     * attachmentText, & attachmentFileName
     */
    public static void sendMessageWithTextAttachment(String smtpServerAddress,
                                                     String fromAddress,
                                                     String toAddress,
                                                     String subject,
                                                     String message,
                                                     String attachmentText,
                                                     String attachmentFileName)
            throws MessagingException {

        if (smtpServerAddress == null || toAddress == null ||
                fromAddress == null || attachmentText == null || attachmentFileName == null) {
            return;
        }
        if (subject == null) {
            subject = " ";
        }
        if (message == null) {
            message = " ";
        }
        SendMail mail = new SendMail(smtpServerAddress, fromAddress, toAddress);
        mail.setSubject(subject);
        mail.setMessage(message);
        mail.addAttachedTextFileFromString(attachmentText, attachmentFileName);
        mail.postMail();
    }

    /**
     * Static method for convenience
     * Sends mail with a single attached File, grabbed from the filesystem
     * Must provide at least smtpServerAddress, fromAddress, toAddress, and
     * attachmentFilePath
     */
    public static void sendMessageWithFileAttachment(String smtpServerAddress,
                                                     String fromAddress,
                                                     String toAddress,
                                                     String subject,
                                                     String message,
                                                     String attachmentFilePath)
            throws MessagingException {

        if (smtpServerAddress == null || toAddress == null || fromAddress == null ||
                attachmentFilePath == null) {
            return;
        }
        if (subject == null) {
            subject = " ";
        }
        if (message == null) {
            message = " ";
        }
        SendMail mail = new SendMail(smtpServerAddress, fromAddress, toAddress);
        mail.setSubject(subject);
        mail.setMessage(message);
        mail.addAttachmentFromFile(attachmentFilePath);
        mail.postMail();
    }

    private void readFile(File file) {
        if (file.exists()) {
            Properties config = new Properties();
            try {
                config.load(new FileInputStream(file));
                parseProperties(config);
            } catch (FileNotFoundException ex) {
                addError(ex.toString());
            } catch (IOException ex) {
                addError(ex.toString());
            }
        } else {
            addError("Configuration file " + file.getPath() + " cannot be found");
        }
    }

    private String[] addRecipient(String[] recipients, String newRecipient) {
        ArrayList list = new ArrayList();
        if (recipients != null && recipients.length > 0) {
            list.addAll(Arrays.asList(recipients));
        }
        list.add(newRecipient);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public void addTO(String newRecipient) {
        recipientsTO = addRecipient(recipientsTO, newRecipient);
    }

    public void addCC(String newRecipient) {
        recipientsCC = addRecipient(recipientsCC, newRecipient);
    }

    public void addBCC(String newRecipient) {
        recipientsBCC = addRecipient(recipientsBCC, newRecipient);
    }

    private void parseProperties(Properties config) {
        SMTP_Host = (String) config.get(SMTP_KEY);
        from = (String) config.get(FROM_KEY);
        if (SMTP_Host != null && from != null) {
            recipientsTO = parseRecipients(config, TO_KEY);
            recipientsCC = parseRecipients(config, CC_KEY);
            recipientsBCC = parseRecipients(config, BCC_KEY);
        }
    }

    private String[] parseRecipients(Properties config, String key) {
        String list = (String) config.get(key);
        if (list != null && list.length() > 0) {
            StringTokenizer st = new StringTokenizer(list, ",");
            String[] recipients = new String[st.countTokens()];
            for (int i = 0; i < recipients.length; i++) {
                recipients[i] = st.nextToken();
            }
            return recipients;
        }
        return null;
    }

    public void setHtmlEnabled(boolean val) {
        htmlEnabled = val;
    }

    public boolean postMail() throws MessagingException {
        Message msg = getMessage();
        if (msg == null) {
            return false;
        }

        if (bodyParts != null) {
            Multipart multipart = new MimeMultipart();
            // set message
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message, htmlEnabled ? "text/html" : "text/plain");
            multipart.addBodyPart(messageBodyPart);
            // add attachments
            for (Iterator i = bodyParts.iterator(); i.hasNext(); ) {
                multipart.addBodyPart((MimeBodyPart) i.next());
            }
            msg.setContent(multipart);
        } else {
            // set Content Type
            msg.setContent(message, htmlEnabled ? "text/html" : "text/plain");
        }
        // send
        Transport.send(msg);
        return true;
    }

    /**
     * Add an attachment from a file on the filesystem
     */
    public boolean addAttachmentFromFile(String attachmentFilePath)
            throws MessagingException {

        if (attachmentFilePath == null || attachmentFilePath.length() == 0) {
            addError("Attachment file path must be longer than zero characters.");
            return false;
        }
        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachmentFilePath);
        attachmentBodyPart.setDataHandler(new DataHandler(source));
        String separator = System.getProperty("file.separator");
        if (separator == null) {
            separator = "";
        }
        int lastSeparator = attachmentFilePath.lastIndexOf(separator);
        if (lastSeparator < 0 || lastSeparator == attachmentFilePath.length() - 1) {
            attachmentBodyPart.setFileName(attachmentFilePath);
        } else {
            attachmentBodyPart.setFileName(attachmentFilePath.substring(lastSeparator + 1));
        }
        if (bodyParts == null) {
            bodyParts = new ArrayList();
        }
        bodyParts.add(attachmentBodyPart);
        return true;
    }

    /**
     * Add an attachment (shows up in email as a text file) with provided
     * text contents (attachmentText) and filename (attachmentFileName)
     */
    public boolean addAttachedTextFileFromString(String attachmentText,
                                                 String attachmentFileName)
            throws MessagingException {

        if (attachmentText == null || attachmentText.length() == 0) {
            addError("Attachment text must be longer than zero characters.");
            return false;
        }
        if (attachmentFileName == null || attachmentFileName.length() == 0) {
            addError("Attachment file name must be longer than zero characters.");
            return false;
        }
        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setText(attachmentText);
        attachmentBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        attachmentBodyPart.setDisposition("attachment");
        attachmentBodyPart.setFileName(attachmentFileName);
        if (bodyParts == null) {
            bodyParts = new ArrayList();
        }
        bodyParts.add(attachmentBodyPart);
        return true;
    }

    /**
     * Creates message from info. in instance variables
     */
    private Message getMessage() throws MessagingException {
        if (subject == null || subject.length() == 0) {
            addError("Subject must be longer than zero characters.");
            return null;
        }
        if (message == null || message.length() == 0) {
            addError("Message must be longer than zero characters: " + message);
            return null;
        }

        boolean debug = false;

        //Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_Host);

        // create some properties and get the default Session
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);

        // create a message
        Message msg = new MimeMessage(session);

        // set the from
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        // set the "to" list
        InternetAddress[] addressTo = getAddresses(recipientsTO);
        if (addressTo != null) {
            msg.setRecipients(Message.RecipientType.TO, addressTo);
        }

        // set the "cc" list
        InternetAddress[] addressCC = getAddresses(recipientsCC);
        if (addressCC != null) {
            msg.setRecipients(Message.RecipientType.CC, addressCC);
        }

        // set the "bcc" list
        InternetAddress[] addressBCC = getAddresses(recipientsBCC);
        if (addressBCC != null) {
            msg.setRecipients(Message.RecipientType.BCC, addressBCC);
        }

        // Optional : You can also set your custom headers in the Email if you Want
        msg.addHeader("MyHeaderName", "myHeaderValue");

        // Setting the Subject
        msg.setSubject(subject);

        return msg;
    }

    private InternetAddress[] getAddresses(String[] recipients)
            throws AddressException {

        if (recipients != null) {
            InternetAddress[] addressTo = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addressTo[i] = new InternetAddress(recipients[i]);
            }
            return addressTo;
        }
        return null;
    }

    public static String normalizeEmail(String email) {
        email = email.trim();
        String ret = "";
        if (email != null) {
            int atSign = email.indexOf('@');
            int dot = email.indexOf('.', atSign);

            // Must have one and only one @ sign
            if (atSign > 0 && atSign < (email.length() - 1) &&
                    email.indexOf('@', atSign + 1) < 0) {

                // Must have a dot after the at sign
                if (dot > atSign) {
                    ret = email;
                }
            }
        }
        return ret;
    }

    public static final String CFGFILE = "-cfgFile";
    public static final String MSGFILE = "-msgFile";
    public static final String SUBJECT = "-s";
    public static final String MESSAGE = "-m";

    public static String[] reqArgs = {CFGFILE};
}