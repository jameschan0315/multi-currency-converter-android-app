package com.currencywiki.currencyconverter.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.widget.AppCompatTextView
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.MainActivity
import com.currencywiki.currencyconverter.common.isDarkMode
import java.util.*


class InfoPageFragment : BasePageFragment(InfoPageFragment::class.simpleName) {

    var type = 0 // 0: Terms and Conditions, 1: Privacy Policy
    private lateinit var titleText: AppCompatTextView
    private lateinit var webView: WebView
    private lateinit var contentText: AppCompatTextView
    var scrollToBottom = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info_page, container, false)
        titleText = view.findViewById(R.id.txt_title)
        contentText = view.findViewById(R.id.txt_content)
        webView = view.findViewById(R.id.webView)
        val settings: WebSettings = webView.settings
        settings.minimumFontSize = 12
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        }
        webView.setPadding(16, 16, 16, 16)
        webView.setBackgroundColor(Color.TRANSPARENT)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.pluginState = WebSettings.PluginState.ON
        settings.mediaPlaybackRequiresUserGesture = false
        webView.webChromeClient = WebChromeClient()
        when (type) {
            0 -> {
                titleText.setText(R.string.terms_conditions_title)
                contentText.setText(R.string.terms_desc)
            }
            1 -> {
                titleText.setText(R.string.privacy_policy_title)

                contentText.setText(R.string.privacy_desc)
            }
            2 -> {
                titleText.setText(R.string.support_title)

                contentText.movementMethod = LinkMovementMethod.getInstance()
            }
        }
        val pish = if (isDarkMode()) {
            "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;" +
                    "src: url(\"file:///android_asset/font/avenir_medium.otf\")}" +
                    "body {color: #444;font-family: MyFont;font-size: 14px;}</style></head><body>"

        } else {
            "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;" +
                    "src: url(\"file:///android_asset/font/avenir_medium.otf\")}" +
                    "body {color: #fff;font-family: MyFont;font-size: 14px;}</style></head><body>"
        }
        val pas = "</body></html>"
        when (type) {
            0 -> {
                val newHead = pish + html_terms + pas
                webView.loadDataWithBaseURL(null, newHead, "text/html", "UTF-8", null)

            }
            1 -> {
                val newHead = pish + html_policy + pas
                webView.loadDataWithBaseURL(null, newHead, "text/html", "UTF-8", null)

            }
            2 -> {
                val newHead = pish + html_support + pas
                webView.loadDataWithBaseURL(null, newHead, "text/html", "UTF-8", null)
               // if (MainActivity.isSettingWidgets) {
               //     MainActivity.isSettingWidgets = false
               //     webView.findAllAsync("How to add widget?");
               // }
            }
        }
        applySetting()
        return view
    }

    override fun onResume() {
        super.onResume()
        if (scrollToBottom) {
            Handler().postDelayed({
                scrollToBottom()
                scrollToBottom = false
            }, 10)
        }
        applySetting()
    }

    override fun applySetting() {
        super.applySetting()

        if (!this::titleText.isInitialized) {
            return
        }
        val pish = if (isDarkMode()) {
            "<html><head><style type=\"text/css\">" +
                    "body {color: #444;font-family: MyFont;font-size: 14px;}</style></head><body>"

        } else {
            "<html><head><style type=\"text/css\">" +
                    "body {color: #fff;font-family: MyFont;font-size: 14px;}</style></head><body>"
        }
        val pas = "</body></html>"
        when (type) {
            0 -> {
                val newHead = pish + html_terms + pas
                webView.loadDataWithBaseURL(null, newHead, "text/html", "UTF-8", null)

            }
            1 -> {
                val newHead = pish + html_policy + pas
                webView.loadDataWithBaseURL(null, newHead, "text/html", "UTF-8", null)

            }
            2 -> {
                val newHead = pish + html_support + pas
                webView.loadDataWithBaseURL(null, newHead, "text/html", "UTF-8", null)
               // if (MainActivity.isSettingWidgets) {
               //     MainActivity.isSettingWidgets = false
               //     webView.findAllAsync("How to add widget?");
               // }
            }
        }
        if (isDarkMode()) {
            titleText.setTextColor(Color.DKGRAY)
            contentText.setTextColor(Color.DKGRAY)
        } else {
            titleText.setTextColor(Color.WHITE)
            contentText.setTextColor(Color.WHITE)
        }

        if (type == 2) {
            /*val supportDesc = getString(R.string.support_desc)
            val ss = SpannableString(supportDesc)
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    val selectorIntent = Intent(Intent.ACTION_SENDTO)
                    selectorIntent.data = Uri.parse("mailto:")

                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.putExtra(
                        Intent.EXTRA_EMAIL,
                        arrayOf("info@currency.wiki")
                    )
                    emailIntent.selector = selectorIntent
                    val activities =
                        requireActivity().packageManager.queryIntentActivities(emailIntent, 0)
                    val isIntentSafe = activities.size > 0
                    if (isIntentSafe) {
                        startActivity(emailIntent)
                    } else {
                        val alertDialog =
                            AlertDialog.Builder(requireActivity()).create()
                        alertDialog.setMessage("Email app is not installed.")
                        alertDialog.show()
                    }
                }
            }
            val start = 56
            val end = 74
            ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (isDarkMode()) {
                ss.setSpan(
                    ForegroundColorSpan(Color.DKGRAY),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                ss.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            contentText.text = ss*/
        }
    }

    fun scrollToBottom() {
        // containerScrollView.fullScroll(View.FOCUS_DOWN)
    }

    val html_policy = "<h1>Privacy Policy</h1>\n" +
            "<p>Last updated: January 06, 2021</p>\n" +
            "<p>This Privacy Policy describes Our policies and procedures on the collection, use and disclosure of Your information when You use the Service and tells You about Your privacy rights and how the law protects You.</p>\n" +
            "<p>We use Your Personal data to provide and improve the Service. By using the Service, You agree to the collection and use of information in accordance with this Privacy Policy.</p>\n" +
            "<h1>Interpretation and Definitions</h1>\n" +
            "<h2>Interpretation</h2>\n" +
            "<p>The words of which the initial letter is capitalized have meanings defined under the following conditions. The following definitions shall have the same meaning regardless of whether they appear in singular or in plural.</p>\n" +
            "<h2>Definitions</h2>\n" +
            "<p>For the purposes of this Privacy Policy:</p>\n" +
            "<ul>\n" +
            "<li>\n" +
            "<p><strong>Account</strong> means a unique account created for You to access our Service or parts of our Service.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Affiliate</strong> means an entity that controls, is controlled by or is under common control with a party, where &quot;control&quot; means ownership of 50% or more of the shares, equity interest or other securities entitled to vote for election of directors or other managing authority.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Application</strong> means the software program provided by the Company downloaded by You on any electronic device, named Currency Converter App by Currency.Wiki</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Company</strong> (referred to as either &quot;the Company&quot;, &quot;We&quot;, &quot;Us&quot; or &quot;Our&quot; in this Agreement) refers to Currency.Wiki, 122 15th St #431 Del Mar, CA 92014.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Country</strong> refers to: California, United States</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Device</strong> means any device that can access the Service such as a computer, a cellphone or a digital tablet.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Personal Data</strong> is any information that relates to an identified or identifiable individual.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Service</strong> refers to the Application.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Service Provider</strong> means any natural or legal person who processes the data on behalf of the Company. It refers to third-party companies or individuals employed by the Company to facilitate the Service, to provide the Service on behalf of the Company, to perform services related to the Service or to assist the Company in analyzing how the Service is used.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Third-party Social Media Service</strong> refers to any website or any social network website through which a User can log in or create an account to use the Service.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Usage Data</strong> refers to data collected automatically, either generated by the use of the Service or from the Service infrastructure itself (for example, the duration of a page visit).</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>You</strong> means the individual accessing or using the Service, or the company, or other legal entity on behalf of which such individual is accessing or using the Service, as applicable.</p>\n" +
            "</li>\n" +
            "</ul>\n" +
            "<h1>Collecting and Using Your Personal Data</h1>\n" +
            "<h2>Types of Data Collected</h2>\n" +
            "<h3>Personal Data</h3>\n" +
            "<p>While using Our Service, We may ask You to provide Us with certain personally identifiable information that can be used to contact or identify You. Personally identifiable information may include, but is not limited to:</p>\n" +
            "<ul>\n" +
            "<li>\n" +
            "<p>Email address</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p>Usage Data</p>\n" +
            "</li>\n" +
            "</ul>\n" +
            "<h3>Usage Data</h3>\n" +
            "<p>Usage Data is collected automatically when using the Service.</p>\n" +
            "<p>Usage Data may include information such as Your Device's Internet Protocol address (e.g. IP address), browser type, browser version, the pages of our Service that You visit, the time and date of Your visit, the time spent on those pages, unique device identifiers and other diagnostic data.</p>\n" +
            "<p>When You access the Service by or through a mobile device, We may collect certain information automatically, including, but not limited to, the type of mobile device You use, Your mobile device unique ID, the IP address of Your mobile device, Your mobile operating system, the type of mobile Internet browser You use, unique device identifiers and other diagnostic data.</p>\n" +
            "<p>We may also collect information that Your browser sends whenever You visit our Service or when You access the Service by or through a mobile device.</p>\n" +
            "<h2>Use of Your Personal Data</h2>\n" +
            "<p>The Company may use Personal Data for the following purposes:</p>\n" +
            "<ul>\n" +
            "<li>\n" +
            "<p><strong>To provide and maintain our Service</strong>, including to monitor the usage of our Service.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>To manage Your Account:</strong> to manage Your registration as a user of the Service. The Personal Data You provide can give You access to different functionalities of the Service that are available to You as a registered user.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>For the performance of a contract:</strong> the development, compliance and undertaking of the purchase contract for the products, items or services You have purchased or of any other contract with Us through the Service.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>To contact You:</strong> To contact You by email, telephone calls, SMS, or other equivalent forms of electronic communication, such as a mobile application's push notifications regarding updates or informative communications related to the functionalities, products or contracted services, including the security updates, when necessary or reasonable for their implementation.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>To provide You</strong> with news, special offers and general information about other goods, services and events which we offer that are similar to those that you have already purchased or enquired about unless You have opted not to receive such information.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>To manage Your requests:</strong> To attend and manage Your requests to Us.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>For business transfers:</strong> We may use Your information to evaluate or conduct a merger, divestiture, restructuring, reorganization, dissolution, or other sale or transfer of some or all of Our assets, whether as a going concern or as part of bankruptcy, liquidation, or similar proceeding, in which Personal Data held by Us about our Service users is among the assets transferred.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>For other purposes</strong>: We may use Your information for other purposes, such as data analysis, identifying usage trends, determining the effectiveness of our promotional campaigns and to evaluate and improve our Service, products, services, marketing and your experience.</p>\n" +
            "</li>\n" +
            "</ul>\n" +
            "<p>We may share Your personal information in the following situations:</p>\n" +
            "<ul>\n" +
            "<li><strong>With Service Providers:</strong> We may share Your personal information with Service Providers to monitor and analyze the use of our Service, to contact You.</li>\n" +
            "<li><strong>For business transfers:</strong> We may share or transfer Your personal information in connection with, or during negotiations of, any merger, sale of Company assets, financing, or acquisition of all or a portion of Our business to another company.</li>\n" +
            "<li><strong>With Affiliates:</strong> We may share Your information with Our affiliates, in which case we will require those affiliates to honor this Privacy Policy. Affiliates include Our parent company and any other subsidiaries, joint venture partners or other companies that We control or that are under common control with Us.</li>\n" +
            "<li><strong>With business partners:</strong> We may share Your information with Our business partners to offer You certain products, services or promotions.</li>\n" +
            "<li><strong>With other users:</strong> when You share personal information or otherwise interact in the public areas with other users, such information may be viewed by all users and may be publicly distributed outside. If You interact with other users or register through a Third-Party Social Media Service, Your contacts on the Third-Party Social Media Service may see Your name, profile, pictures and description of Your activity. Similarly, other users will be able to view descriptions of Your activity, communicate with You and view Your profile.</li>\n" +
            "<li><strong>With Your consent</strong>: We may disclose Your personal information for any other purpose with Your consent.</li>\n" +
            "</ul>\n" +
            "<h2>Retention of Your Personal Data</h2>\n" +
            "<p>The Company will retain Your Personal Data only for as long as is necessary for the purposes set out in this Privacy Policy. We will retain and use Your Personal Data to the extent necessary to comply with our legal obligations (for example, if we are required to retain your data to comply with applicable laws), resolve disputes, and enforce our legal agreements and policies.</p>\n" +
            "<p>The Company will also retain Usage Data for internal analysis purposes. Usage Data is generally retained for a shorter period of time, except when this data is used to strengthen the security or to improve the functionality of Our Service, or We are legally obligated to retain this data for longer time periods.</p>\n" +
            "<h2>Transfer of Your Personal Data</h2>\n" +
            "<p>Your information, including Personal Data, is processed at the Company's operating offices and in any other places where the parties involved in the processing are located. It means that this information may be transferred to — and maintained on — computers located outside of Your state, province, country or other governmental jurisdiction where the data protection laws may differ than those from Your jurisdiction.</p>\n" +
            "<p>Your consent to this Privacy Policy followed by Your submission of such information represents Your agreement to that transfer.</p>\n" +
            "<p>The Company will take all steps reasonably necessary to ensure that Your data is treated securely and in accordance with this Privacy Policy and no transfer of Your Personal Data will take place to an organization or a country unless there are adequate controls in place including the security of Your data and other personal information.</p>\n" +
            "<h2>Disclosure of Your Personal Data</h2>\n" +
            "<h3>Business Transactions</h3>\n" +
            "<p>If the Company is involved in a merger, acquisition or asset sale, Your Personal Data may be transferred. We will provide notice before Your Personal Data is transferred and becomes subject to a different Privacy Policy.</p>\n" +
            "<h3>Law enforcement</h3>\n" +
            "<p>Under certain circumstances, the Company may be required to disclose Your Personal Data if required to do so by law or in response to valid requests by public authorities (e.g. a court or a government agency).</p>\n" +
            "<h3>Other legal requirements</h3>\n" +
            "<p>The Company may disclose Your Personal Data in the good faith belief that such action is necessary to:</p>\n" +
            "<ul>\n" +
            "<li>Comply with a legal obligation</li>\n" +
            "<li>Protect and defend the rights or property of the Company</li>\n" +
            "<li>Prevent or investigate possible wrongdoing in connection with the Service</li>\n" +
            "<li>Protect the personal safety of Users of the Service or the public</li>\n" +
            "<li>Protect against legal liability</li>\n" +
            "</ul>\n" +
            "<h2>Security of Your Personal Data</h2>\n" +
            "<p>The security of Your Personal Data is important to Us, but remember that no method of transmission over the Internet, or method of electronic storage is 100% secure. While We strive to use commercially acceptable means to protect Your Personal Data, We cannot guarantee its absolute security.</p>\n" +
            "<h1>Detailed Information on the Processing of Your Personal Data</h1>\n" +
            "<p>Service Providers have access to Your Personal Data only to perform their tasks on Our behalf and are obligated not to disclose or use it for any other purpose.</p>\n" +
            "<h2>Analytics</h2>\n" +
            "<p>We may use third-party Service providers to monitor and analyze the use of our Service.</p>\n" +
            "<ul>\n" +
            "<li>\n" +
            "<p><strong>Google Analytics</strong></p>\n" +
            "<p>Google Analytics is a web analytics service offered by Google that tracks and reports website traffic. Google uses the data collected to track and monitor the use of our Service. This data is shared with other Google services. Google may use the collected data to contextualize and personalize the ads of its own advertising network.</p>\n" +
            "<p>You may opt-out of certain Google Analytics features through your mobile device settings, such as your device advertising settings or by following the instructions provided by Google in their Privacy Policy: <a href=\"https://policies.google.com/privacy\" rel=\"external nofollow noopener\" target=\"_blank\">https://policies.google.com/privacy</a></p>\n" +
            "<p>For more information on the privacy practices of Google, please visit the Google Privacy &amp; Terms web page: <a href=\"https://policies.google.com/privacy\" rel=\"external nofollow noopener\" target=\"_blank\">https://policies.google.com/privacy</a></p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Firebase</strong></p>\n" +
            "<p>Firebase is an analytics service provided by Google Inc.</p>\n" +
            "<p>You may opt-out of certain Firebase features through your mobile device settings, such as your device advertising settings or by following the instructions provided by Google in their Privacy Policy: <a href=\"https://policies.google.com/privacy\" rel=\"external nofollow noopener\" target=\"_blank\">https://policies.google.com/privacy</a></p>\n" +
            "<p>We also encourage you to review the Google's policy for safeguarding your data: <a href=\"https://support.google.com/analytics/answer/6004245\" rel=\"external nofollow noopener\" target=\"_blank\">https://support.google.com/analytics/answer/6004245</a></p>\n" +
            "<p>For more information on what type of information Firebase collects, please visit the How Google uses data when you use our partners' sites or apps webpage: <a href=\"https://policies.google.com/technologies/partner-sites\" rel=\"external nofollow noopener\" target=\"_blank\">https://policies.google.com/technologies/partner-sites</a></p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>AppsFlyer</strong></p>\n" +
            "<p>For more information on the privacy: <a href=\"https://www.appsflyer.com/privacy-policy/\" rel=\"external nofollow noopener\" target=\"_blank\">https://www.appsflyer.com/privacy-policy/</a></p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Facebook App Ads</strong></p>\n" +
            "<p>For more information on the privacy: <a href=\"https://www.facebook.com/about/privacy\" rel=\"external nofollow noopener\" target=\"_blank\">https://www.facebook.com/about/privacy</a></p>\n" +
            "</li>\n" +
            "</ul>\n" +
            "<h1>Your California Privacy Rights (California's Shine the Light law)</h1>\n" +
            "<p>Under California Civil Code Section 1798 (California's Shine the Light law), California residents with an established business relationship with us can request information once a year about sharing their Personal Data with third parties for the third parties' direct marketing purposes.</p>\n" +
            "<p>If you'd like to request more information under the California Shine the Light law, and if You are a California resident, You can contact Us using the contact information provided below.</p>\n" +
            "<h1>California Privacy Rights for Minor Users (California Business and Professions Code Section 22581)</h1>\n" +
            "<p>California Business and Professions Code section 22581 allow California residents under the age of 18 who are registered users of online sites, services or applications to request and obtain removal of content or information they have publicly posted.</p>\n" +
            "<p>To request removal of such data, and if You are a California resident, You can contact Us using the contact information provided below, and include the email address associated with Your account.</p>\n" +
            "<p>Be aware that Your request does not guarantee complete or comprehensive removal of content or information posted online and that the law may not permit or require removal in certain circumstances.</p>\n" +
            "<h1>Links to Other Websites</h1>\n" +
            "<p>Our Service may contain links to other websites that are not operated by Us. If You click on a third party link, You will be directed to that third party's site. We strongly advise You to review the Privacy Policy of every site You visit.</p>\n" +
            "<p>We have no control over and assume no responsibility for the content, privacy policies or practices of any third party sites or services.</p>\n" +
            "<h1>Changes to this Privacy Policy</h1>\n" +
            "<p>We may update Our Privacy Policy from time to time. We will notify You of any changes by posting the new Privacy Policy on this page.</p>\n" +
            "<p>We will let You know via email and/or a prominent notice on Our Service, prior to the change becoming effective and update the &quot;Last updated&quot; date at the top of this Privacy Policy.</p>\n" +
            "<p>You are advised to review this Privacy Policy periodically for any changes. Changes to this Privacy Policy are effective when they are posted on this page.</p>\n" +
            "<h1>Contact Us</h1>\n" +
            "<p>If you have any questions about this Privacy Policy, You can contact us:</p>\n" +
            "<ul>\n" +
            "<li>By email: info@currency.wiki</li>\n" +
            "</ul>"
    val html_terms = "<h1>Terms and Conditions</h1>\n" +
            "<p>Last updated: November 04, 2020</p>\n" +
            "<p>Please read these terms and conditions carefully before using Our Service.</p>\n" +
            "<h1>Interpretation and Definitions</h1>\n" +
            "<h2>Interpretation</h2>\n" +
            "<p>The words of which the initial letter is capitalized have meanings defined under the following conditions. The following definitions shall have the same meaning regardless of whether they appear in singular or in plural.</p>\n" +
            "<h2>Definitions</h2>\n" +
            "<p>For the purposes of these Terms and Conditions:</p>\n" +
            "<ul>\n" +
            "<li>\n" +
            "<p><strong>Application</strong> means the software program provided by the Company downloaded by You on any electronic device, named Currency Converter App by Currency.Wiki</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Application Store</strong> means the digital distribution service operated and developed by Apple Inc. (Apple App Store) or Google Inc. (Google Play Store) in which the Application has been downloaded.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Affiliate</strong> means an entity that controls, is controlled by or is under common control with a party, where &quot;control&quot; means ownership of 50% or more of the shares, equity interest or other securities entitled to vote for election of directors or other managing authority.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Country</strong> refers to: California, United States</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Company</strong> (referred to as either &quot;the Company&quot;, &quot;We&quot;, &quot;Us&quot; or &quot;Our&quot; in this Agreement) refers to Currency.Wiki, 122 15th st, #431 Del Mar, CA 92014.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Device</strong> means any device that can access the Service such as a computer, a cellphone or a digital tablet.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Service</strong> refers to the Application.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Terms and Conditions</strong> (also referred as &quot;Terms&quot;) mean these Terms and Conditions that form the entire agreement between You and the Company regarding the use of the Service.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>Third-party Social Media Service</strong> means any services or content (including data, information, products or services) provided by a third-party that may be displayed, included or made available by the Service.</p>\n" +
            "</li>\n" +
            "<li>\n" +
            "<p><strong>You</strong> means the individual accessing or using the Service, or the company, or other legal entity on behalf of which such individual is accessing or using the Service, as applicable.</p>\n" +
            "</li>\n" +
            "</ul>\n" +
            "<h1>Acknowledgment</h1>\n" +
            "<p>These are the Terms and Conditions governing the use of this Service and the agreement that operates between You and the Company. These Terms and Conditions set out the rights and obligations of all users regarding the use of the Service.</p>\n" +
            "<p>Your access to and use of the Service is conditioned on Your acceptance of and compliance with these Terms and Conditions. These Terms and Conditions apply to all visitors, users and others who access or use the Service.</p>\n" +
            "<p>By accessing or using the Service You agree to be bound by these Terms and Conditions. If You disagree with any part of these Terms and Conditions then You may not access the Service.</p>\n" +
            "<p>You represent that you are over the age of 18. The Company does not permit those under 18 to use the Service.</p>\n" +
            "<p>Your access to and use of the Service is also conditioned on Your acceptance of and compliance with the Privacy Policy of the Company. Our Privacy Policy describes Our policies and procedures on the collection, use and disclosure of Your personal information when You use the Application or the Website and tells You about Your privacy rights and how the law protects You. Please read Our Privacy Policy carefully before using Our Service.</p>\n" +
            "<h1>Intellectual Property</h1>\n" +
            "<p>The Service and its original content (excluding Content provided by You or other users), features and functionality are and will remain the exclusive property of the Company and its licensors.</p>\n" +
            "<p>The Service is protected by copyright, trademark, and other laws of both the Country and foreign countries.</p>\n" +
            "<p>Our trademarks and trade dress may not be used in connection with any product or service without the prior written consent of the Company.</p>\n" +
            "<h1>Links to Other Websites</h1>\n" +
            "<p>Our Service may contain links to third-party web sites or services that are not owned or controlled by the Company.</p>\n" +
            "<p>The Company has no control over, and assumes no responsibility for, the content, privacy policies, or practices of any third party web sites or services. You further acknowledge and agree that the Company shall not be responsible or liable, directly or indirectly, for any damage or loss caused or alleged to be caused by or in connection with the use of or reliance on any such content, goods or services available on or through any such web sites or services.</p>\n" +
            "<p>We strongly advise You to read the terms and conditions and privacy policies of any third-party web sites or services that You visit.</p>\n" +
            "<h1>Termination</h1>\n" +
            "<p>We may terminate or suspend Your access immediately, without prior notice or liability, for any reason whatsoever, including without limitation if You breach these Terms and Conditions.</p>\n" +
            "<p>Upon termination, Your right to use the Service will cease immediately.</p>\n" +
            "<h1>Limitation of Liability</h1>\n" +
            "<p>Notwithstanding any damages that You might incur, the entire liability of the Company and any of its suppliers under any provision of this Terms and Your exclusive remedy for all of the foregoing shall be limited to the amount actually paid by You through the Service or 100 USD if You haven't purchased anything through the Service.</p>\n" +
            "<p>To the maximum extent permitted by applicable law, in no event shall the Company or its suppliers be liable for any special, incidental, indirect, or consequential damages whatsoever (including, but not limited to, damages for loss of profits, loss of data or other information, for business interruption, for personal injury, loss of privacy arising out of or in any way related to the use of or inability to use the Service, third-party software and/or third-party hardware used with the Service, or otherwise in connection with any provision of this Terms), even if the Company or any supplier has been advised of the possibility of such damages and even if the remedy fails of its essential purpose.</p>\n" +
            "<p>Some states do not allow the exclusion of implied warranties or limitation of liability for incidental or consequential damages, which means that some of the above limitations may not apply. In these states, each party's liability will be limited to the greatest extent permitted by law.</p>\n" +
            "<h1>&quot;AS IS&quot; and &quot;AS AVAILABLE&quot; Disclaimer</h1>\n" +
            "<p>The Service is provided to You &quot;AS IS&quot; and &quot;AS AVAILABLE&quot; and with all faults and defects without warranty of any kind. To the maximum extent permitted under applicable law, the Company, on its own behalf and on behalf of its Affiliates and its and their respective licensors and service providers, expressly disclaims all warranties, whether express, implied, statutory or otherwise, with respect to the Service, including all implied warranties of merchantability, fitness for a particular purpose, title and non-infringement, and warranties that may arise out of course of dealing, course of performance, usage or trade practice. Without limitation to the foregoing, the Company provides no warranty or undertaking, and makes no representation of any kind that the Service will meet Your requirements, achieve any intended results, be compatible or work with any other software, applications, systems or services, operate without interruption, meet any performance or reliability standards or be error free or that any errors or defects can or will be corrected.</p>\n" +
            "<p>Without limiting the foregoing, neither the Company nor any of the company's provider makes any representation or warranty of any kind, express or implied: (i) as to the operation or availability of the Service, or the information, content, and materials or products included thereon; (ii) that the Service will be uninterrupted or error-free; (iii) as to the accuracy, reliability, or currency of any information or content provided through the Service; or (iv) that the Service, its servers, the content, or e-mails sent from or on behalf of the Company are free of viruses, scripts, trojan horses, worms, malware, timebombs or other harmful components.</p>\n" +
            "<p>Some jurisdictions do not allow the exclusion of certain types of warranties or limitations on applicable statutory rights of a consumer, so some or all of the above exclusions and limitations may not apply to You. But in such a case the exclusions and limitations set forth in this section shall be applied to the greatest extent enforceable under applicable law.</p>\n" +
            "<h1>Governing Law</h1>\n" +
            "<p>The laws of the Country, excluding its conflicts of law rules, shall govern this Terms and Your use of the Service. Your use of the Application may also be subject to other local, state, national, or international laws.</p>\n" +
            "<h1>Disputes Resolution</h1>\n" +
            "<p>If You have any concern or dispute about the Service, You agree to first try to resolve the dispute informally by contacting the Company.</p>\n" +
            "<h1>For European Union (EU) Users</h1>\n" +
            "<p>If You are a European Union consumer, you will benefit from any mandatory provisions of the law of the country in which you are resident in.</p>\n" +
            "<h1>United States Legal Compliance</h1>\n" +
            "<p>You represent and warrant that (i) You are not located in a country that is subject to the United States government embargo, or that has been designated by the United States government as a &quot;terrorist supporting&quot; country, and (ii) You are not listed on any United States government list of prohibited or restricted parties.</p>\n" +
            "<h1>Severability and Waiver</h1>\n" +
            "<h2>Severability</h2>\n" +
            "<p>If any provision of these Terms is held to be unenforceable or invalid, such provision will be changed and interpreted to accomplish the objectives of such provision to the greatest extent possible under applicable law and the remaining provisions will continue in full force and effect.</p>\n" +
            "<h2>Waiver</h2>\n" +
            "<p>Except as provided herein, the failure to exercise a right or to require performance of an obligation under this Terms shall not effect a party's ability to exercise such right or require such performance at any time thereafter nor shall be the waiver of a breach constitute a waiver of any subsequent breach.</p>\n" +
            "<h1>Translation Interpretation</h1>\n" +
            "<p>These Terms and Conditions may have been translated if We have made them available to You on our Service.\n" +
            "You agree that the original English text shall prevail in the case of a dispute.</p>\n" +
            "<h1>Changes to These Terms and Conditions</h1>\n" +
            "<p>We reserve the right, at Our sole discretion, to modify or replace these Terms at any time. If a revision is material We will make reasonable efforts to provide at least 30 days' notice prior to any new terms taking effect. What constitutes a material change will be determined at Our sole discretion.</p>\n" +
            "<p>By continuing to access or use Our Service after those revisions become effective, You agree to be bound by the revised terms. If You do not agree to the new terms, in whole or in part, please stop using the website and the Service.</p>\n" +
            "<h1>Disclaimer</h1>\n" +
            "<p>Currency.wiki exchange rates are for informational purposes only. Please verify/confirm currency rates with your forex broker or financial institution before making international money transfers and transactions.</p>\n" +
            "<h1>Contact Us</h1>\n" +
            "<p>If you have any questions about these Terms and Conditions, You can contact us:</p>\n" +
            "<ul>\n" +
            "<li>By email: info@currency.wiki</li>\n" +
            "</ul>"
    val html_support = "<h1>Support</h1>\n" +
            "<p>If you're having an issue with our currency converter app, please email to <a href=\"mailto:app@currency.wiki\">app@currency.wiki</a> (allow 24-48 hours for a response) and in the subject line, please add \"Issue with app\" </p>\n" +
            "<p>Please describe the issue, including your device's name and the model number, and the current android version. e.g., Samsung Galaxy S9+ Android 10</p>\n" +
            "<h1>FAQ</h1>\n" +
            "<h2>How often exchange rates update?</h2>\n" +
            "<p>Rates are updated every 30 minutes and in-time every 60 seconds except weekends (Forex market in the US closes on Friday at 5 PM EST and opens on Sunday at 5 PM EST)</p> \n" +
            "<h2>How often cryptocurrencies update?</h2>\n" +
            "<p>The cryptocurrency market never closes. It is open 24/7, 365 days a year. Bitcoin (BTC) rates are always updated, even on weekends </p> \n" +
            "<h2>How to add widget?</h2>\n" +
            "<ul>\n" +
            "<li>On your home screen, touch and hold an empty spot</li><li>Tap on \"Widgets\"</li><li>In the search box, type \"CurrencyConverter\" and it should display a currency widget</li><li>You tap on the widget, and it will prompt you to select the type of widget you want to display by holding and dragging to your home screen</li>\n" +
            "</ul>\n" +
            "<h2>Why do I get one of the following system notifications?</h2>\n" +
            "<ul>\n" +
            "<li>Stop optimizing battery usage</li><li>Do not optimize battery usage</li><li>Let app always run in background</li><li>Ignore battery optimizations</li>\n" +
            "</ul>\n" +
            "<p>If you are using a currency widget on your home screen, we recommend that you allow our app to run in the background to ensure rates are always up to date</p>\n" +
            "<h2>Battery usage?</h2>\n" +
            "<p>Our app was designed to use a minimum amount of battery, and when operating in the background to update rates. Please note that many factors come into play, such as closing the app after usage, making sure your device is compatible with our app, etc...</p>\n" +
            "<h1>Tips</h1>\n" +
            "<h2>Copy to share</h2>\n" +
            "<p>If you go to the main currency converter screen, tap, and hold results. It will copy results into the clipboard as an example, \"1.00 USD = 0.84 EUR\" that you can share</p>\n" +
            "<h2>Calculate tips/fees/taxes</h2>\n" +
            "<p>We've created the following logic for quick tip calculations; if you enter example \"50%10\" and tap \"=\" it will calculate 10% from 50</p>\n" +
            "<h2>Multi-converter</h2>\n" +
            "<p>You may re-arrange the order of currencies in your selected list by dragging and dropping</p>"
}