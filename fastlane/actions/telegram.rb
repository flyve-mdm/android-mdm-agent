module Fastlane
  module Actions
      class TelegramAction < Action
          def self.run(params)
            require 'net/http'
            require 'uri'

            uri = URI.parse(params[:url_horn])
            https = Net::HTTP.new(uri.host, uri.port)
            https.use_ssl = true

            req = Net::HTTP::Post.new(uri.request_uri)
            req.set_form_data({
              'payload' => '{ "text": "' + params[:message] + "\\n- *Repo*: " + params[:repo] + "\\n- *Branch*: " + params[:branch] + "\\n- *Author*: " + Actions.git_author_email + '" }'
            })

            response = https.request(req)
            UI.message(response)
           end

             #####################################################
             # @!group Documentation
             #####################################################

             def self.description
               "Telegram send message to telegram with @bullhorn_bot"
             end

             def self.available_options
               [
                 FastlaneCore::ConfigItem.new(key: :url_horn,
                                              env_name: "URL_HORN",
                                              description: "Path from @bullhorn_bot on Telegram",
                                              is_string: true),
                 FastlaneCore::ConfigItem.new(key: :message,
                                              env_name: "MESSAGE",
                                              description: "Text to send",
                                              is_string: true),
                 FastlaneCore::ConfigItem.new(key: :repo,
                                              env_name: "REPO",
                                              description: "Get github repository",
                                              is_string: true),
                 FastlaneCore::ConfigItem.new(key: :branch,
                                              env_name: "BRANCH",
                                              description: "Get github branch",
                                              is_string: true)
               ]
             end

             def self.authors
               ["rafaelje"]
             end

             def self.is_supported?(platform)
               true
             end
       end
  end
end