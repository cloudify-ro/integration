<?php

require_once __DIR__ . "/client.php";

global $os;

const IMAGE_NAME = 'minimal-ubuntu-20.04'; // Ubuntu 20.04 LTS
const FLAVOR_NAME = 'm1.g2.4'; // 2 vCPU 4GB RAM
const NETWORK = 'public'; // the public IPv4 network

// Get the image
$image = $os->imagesV2()->listImages(['name' => IMAGE_NAME])->current();
// Get the instance flavor
$flavor = $os->computeV2()->listFlavors(['name' => FLAVOR_NAME])->current();
// Get the network
$network = $os->networkingV2()->listNetworks(['name' => NETWORK])->current();
// Get the SSH key (must be created first)
$key = $os->computeV2()->listKeypairs()->current();

// create a Server
$server = $os->computeV2()->createServer([
    'name' => 'my-first-server',
    'imageId' => $image->id,
    'flavorId' => $flavor->id,
    'networks' => [
        ['uuid' => $network->id]
    ],
    'keyName' => $key->name
]);

// then delete the server
// $server->delete();
