require 'net/ssh'
require "highline/import"

USERNAME = "tescoproductprice"
SERVER = "productsvc-qa.cloudapp.net"
PORT = 22

#USERNAME = "azureuser"
#SERVER = "productsvc-qa.cloudapp.net"
#PORT = 63138

#USERNAME = "azureuser"
#SERVER = "productsvc-qa.cloudapp.net"
#PORT = 51882

def stop_service_on_qa ssh
  begin
    ssh.exec! "cd /tmp/priceService && kill -9 $(cat priceService.pid)"
  rescue
    puts "-"*20
    puts "unable to stop - service may already be stopped"
    puts "carry on cleaning /tmp/service..."
    puts "-"*20
  end
  ssh.exec! "rm -rf /tmp/priceService/*"
end

def exit_script message
  puts message
  exit 1
end

def parse_filename filepath
  match = filepath.match(/.*\/(.*).zip/)
  match.captures.first unless match.nil?
end

if ARGV.length != 1
  exit_script "Usage: ruby deploy_to_qa.rb <downloaded service zip file>"
end


begin

  filepath = ARGV.first
  filename = parse_filename filepath

  password = ask("SSH password for #{USERNAME}@#{SERVER}: ") { |input| input.echo = false }

  puts "*"*100
  puts "ssh into machine..."
  Net::SSH.start(SERVER, USERNAME, :password => password, :port => PORT) do |ssh|
    puts "ssh success!"

    puts "*"*100
    puts "creating folder structures if not exist..."
    ssh.exec! "mkdir -p /tmp/priceService"

    puts "*"*100
    puts "stopping running service and cleaning old artifacts..."
    stop_service_on_qa ssh

    puts "*"*100
    puts "copying file to qa environment..."
    `scp -P #{PORT} #{filepath} #{USERNAME}@#{SERVER}:/tmp/priceService/ > /dev/tty`

    puts "*"*100
    puts "unzipping archive..."
    ssh.exec!("cd /tmp/priceService && unzip #{filename}.zip") do |channel, stream, line|
      puts line
    end

    puts "*"*100
    puts "starting service..."
    ssh.exec!("cd /tmp/priceService && nohup sh runService qa #{filename}.jar > output.txt 2> error.txt < /dev/null &") do |channel, stream, line|
      puts line
    end

    result = ssh.exec! "echo $?"
    puts "*"*100
    if result.to_i != 0
      exit_script "SOMETHING WENT WRONG" if result != 0
    end
    puts "SERVICE STARTED! Have a nice day :)"
  end

rescue Exception => exception
  exit_script exception.inspect
end

