#!/bin/bash

# Kafka Topics Management Script
# Wrapper for Docker-based Kafka topic operations

BROKER_CONTAINER="broker"
BOOTSTRAP_SERVER="localhost:9092"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Kafka container is running
check_kafka() {
    if ! docker ps | grep -q "$BROKER_CONTAINER"; then
        echo -e "${RED}Error: Kafka container '$BROKER_CONTAINER' is not running${NC}"
        echo "Please start Kafka first"
        exit 1
    fi
}

# Show usage
show_usage() {
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  --list                     List all topics"
    echo "  --create                   Create a new topic"
    echo "  --delete                   Delete a topic"
    echo "  --describe                 Describe a topic"
    echo "  --help                     Show this help"
    echo ""
    echo "Create Options:"
    echo "  --topic <name>             Topic name"
    echo "  --partitions <num>         Number of partitions (default: 1)"
    echo "  --replication-factor <num> Replication factor (default: 1)"
    echo ""
    echo "Delete Options:"
    echo "  --topic <name>             Topic name to delete"
    echo ""
    echo "Describe Options:"
    echo "  --topic <name>             Topic name to describe (optional, describes all if omitted)"
    echo ""
    echo "Examples:"
    echo "  $0 --list"
    echo "  $0 --create --topic my-topic --partitions 3 --replication-factor 1"
    echo "  $0 --delete --topic my-topic"
    echo "  $0 --describe --topic my-topic"
}

# List all topics
list_topics() {
    echo -e "${GREEN}Listing Kafka topics:${NC}"
    docker exec "$BROKER_CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP_SERVER" --list
}

# Create a topic
create_topic() {
    local topic=""
    local partitions=1
    local replication_factor=1
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --topic)
                topic="$2"
                shift 2
                ;;
            --partitions)
                partitions="$2"
                shift 2
                ;;
            --replication-factor)
                replication_factor="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                exit 1
                ;;
        esac
    done
    
    if [[ -z "$topic" ]]; then
        echo -e "${RED}Error: Topic name is required for creation${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Creating topic '$topic' with $partitions partitions and replication factor $replication_factor${NC}"
    docker exec "$BROKER_CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP_SERVER" --create --topic "$topic" --partitions "$partitions" --replication-factor "$replication_factor"
}

# Delete a topic
delete_topic() {
    local topic=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --topic)
                topic="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                exit 1
                ;;
        esac
    done
    
    if [[ -z "$topic" ]]; then
        echo -e "${RED}Error: Topic name is required for deletion${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}Deleting topic '$topic'${NC}"
    docker exec "$BROKER_CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP_SERVER" --delete --topic "$topic"
}

# Describe topic(s)
describe_topic() {
    local topic=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --topic)
                topic="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                exit 1
                ;;
        esac
    done
    
    echo -e "${GREEN}Describing topic(s):${NC}"
    if [[ -n "$topic" ]]; then
        docker exec "$BROKER_CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP_SERVER" --describe --topic "$topic"
    else
        docker exec "$BROKER_CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP_SERVER" --describe
    fi
}

# Main script logic
main() {
    check_kafka
    
    if [[ $# -eq 0 ]]; then
        show_usage
        exit 1
    fi
    
    case $1 in
        --list)
            list_topics
            ;;
        --create)
            shift
            create_topic "$@"
            ;;
        --delete)
            shift
            delete_topic "$@"
            ;;
        --describe)
            shift
            describe_topic "$@"
            ;;
        --help)
            show_usage
            ;;
        *)
            echo -e "${RED}Unknown command: $1${NC}"
            show_usage
            exit 1
            ;;
    esac
}

main "$@"