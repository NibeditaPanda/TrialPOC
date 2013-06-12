require 'net/ssh'
require "highline/import"

USERNAME = "tescoproductprice"
SERVER = "productsvc-qa.cloudapp.net"
#USERNAME = "vagrant"
#SERVER = "10.0.2.15"

def secure_copy_to_qa adapter_filepath, csv_filepath
  `scp #{adapter_filepath} #{USERNAME}@#{SERVER}:/tmp/priceAdapters/ > /dev/tty`
  `scp #{csv_filepath} #{USERNAME}@#{SERVER}:/tmp/priceAdapters/ > /dev/tty`
end

def exit_script message
  puts message
  exit 1
end

def parse_filename filepath
  match = filepath.match(/.*\/(.*).zip/)
  match.captures.first unless match.nil?
end

if ARGV.length != 2
  exit_script %^
    Usage: ruby import_data_qa.rb <adapter zip> <csv file zip>
    Note: Remember to set correct values for data dump in qa.properties.
          The files will be extracted to the same location
  ^
end

begin
  adapter_filepath, csv_filepath = ARGV

  adapter_filename = parse_filename adapter_filepath
  csv_match = csv_filepath.match(/.*\/(.*)$/)
  csv_filename = csv_match ? csv_match.captures.first : csv_filepath

  password = ask("SSH password for #{USERNAME}@#{SERVER}: ") { |input| input.echo = false }

  puts "*"*100
  puts "ssh into machine..."
  Net::SSH.start(SERVER, USERNAME, :password => password) do |ssh|
    puts "ssh success!"

    puts "*"*100
    puts "creating folder structures if not exist..."
    ssh.exec! "mkdir -p /tmp/priceAdapters"

    puts "*"*100
    puts "cleaning old artifacts..."
    ssh.exec! "rm -rf /tmp/priceAdapters/* /tmp/to_process/price/*"

    puts "*"*100
    puts "copying file to qa db box..."
    secure_copy_to_qa adapter_filepath, csv_filepath

    puts "*"*100
    puts "unzipping archives..."

    ssh.exec!("cd /tmp/priceAdapters && unzip -o #{csv_filename} -d /tmp/to_process/price && unzip -o #{adapter_filename}.zip") do |channel, stream, line|
      print line
    end

    puts "*"*100
    puts "importing data..."

    ssh.exec!("cd /tmp/priceAdapters && sh runImport qa #{adapter_filename}.jar") do |channel, stream, line|
      print line
    end

    result = ssh.exec! "echo $?"
    puts "*"*100
    if result.to_i != 0
      exit_script "SOMETHING WENT WRONG" if result != 0
    end
    puts "IMPORT FINISHED! Have a nice day :)"
  end

rescue Exception => exception
  exit_script exception.inspect
end

