module Fastlane
  module Actions
      class TelegramAction < Action
          def self.run(params)
            require 'net/http'
            require 'uri'

            UI.message(params[:url_horn])

            uri = URI.parse(params[:url_horn])
            https = Net::HTTP.new(uri.host, uri.port)
            https.use_ssl = true

            req = Net::HTTP::Post.new(uri.request_uri)
            req.set_form_data({
              'payload' => '{ "text": "' + params[:message] + "- *Branch*: " + Actions.git_branch + "- *Author*: " + Actions.git_author_email + '" }'
            })

            response = https.request(req)
            UI.message(response)
           end

             #####################################################
             # @!group Documentation
             #####################################################

             def self.description
               "Telegram send message to telegram with Horn"
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
                                              is_string: true)
               ]
             end

             def self.authors
               ["rafaelje"]
             end

             def self.is_supported?(platform)
               platform == :android
             end
       end
  end
end