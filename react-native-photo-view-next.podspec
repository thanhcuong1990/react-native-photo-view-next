require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = 'react-native-photo-view-next'
  s.version      = package['version']
  s.summary      = package['description']
  s.author       = package['author']
  s.homepage     = package['homepage']
  s.license      = package['license']
  s.ios.deployment_target = '12.0'
  s.tvos.deployment_target = '12.0'
  s.source       = { git: 'https://github.com/thanhcuong1990/react-native-photo-view-next.git', tag: "v#{s.version}" }
  s.source_files = 'ios/*.{h,m}'
  s.dependency 'React-Core'
  s.dependency 'SDWebImage'
  s.dependency 'SDWebImageWebPCoder'
  s.dependency 'SDWebImagePhotosPlugin'
end
